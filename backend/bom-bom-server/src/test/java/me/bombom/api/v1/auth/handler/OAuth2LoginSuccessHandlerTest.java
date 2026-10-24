package me.bombom.api.v1.auth.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.util.UniqueUserInfoGenerator;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2LoginSuccessHandlerTest {

    private static final String FRONTEND_BASE_URL = "https://www.bombom.news";
    private static final Map<String, Object> NEW_MEMBER_ATTRIBUTES = Map.of(
            "sub", "oauth-user",
            "email", "new-member@example.com",
            "name", "new member"
    );

    private OAuth2LoginSuccessHandler handler;
    private UniqueUserInfoGenerator uniqueUserInfoGenerator;
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        uniqueUserInfoGenerator = mock(UniqueUserInfoGenerator.class);
        memberService = mock(MemberService.class);
        handler = new OAuth2LoginSuccessHandler(
                memberService,
                uniqueUserInfoGenerator
        );
        ReflectionTestUtils.setField(handler, "redirectUriWhitelist", List.of(FRONTEND_BASE_URL));
        ReflectionTestUtils.setField(handler, "frontendBaseUrl", FRONTEND_BASE_URL);

        when(uniqueUserInfoGenerator.getUniqueEmailLocalPart("new-member@example.com"))
                .thenReturn("new-member");
        when(uniqueUserInfoGenerator.getUniqueNickname("new member"))
                .thenReturn("new-member");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/blog", "/blog/post/41?from=home"})
    void 미가입_사용자는_로그인_시작_경로와_무관하게_회원가입_페이지로_이동한다(String path) throws Exception {
        MockHttpServletRequest request = requestWithRedirectUrl(FRONTEND_BASE_URL + path);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication(NEW_MEMBER_ATTRIBUTES, null));

        assertThat(response.getRedirectedUrl())
                .isEqualTo(FRONTEND_BASE_URL + "/signup?email=new-member&name=new-member");
    }

    @Test
    void 기존_회원의_리다이렉트_동작은_유지한다() throws Exception {
        String redirectUrl = FRONTEND_BASE_URL + "/blog";
        MockHttpServletRequest request = requestWithRedirectUrl(redirectUrl);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication(Map.of("sub", "oauth-user"), mock(Member.class)));

        assertThat(response.getRedirectedUrl()).isEqualTo(redirectUrl + "/");
    }

    @Test
    void 재인증한_회원이_탈퇴_대상과_다르면_탈퇴하지_않는다() throws Exception {
        var request = requestWithRedirectUrl(FRONTEND_BASE_URL);
        request.getSession().setAttribute("pendingWithdraw", true);
        request.getSession().setAttribute("withdrawMemberId", 1L);
        Member other = mock(Member.class);
        when(other.getId()).thenReturn(2L);
        var response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication(Map.of("sub", "other-user"), other));

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_BASE_URL + "/login?error=reauth_mismatch");
        verify(memberService, never()).withdraw(1L);
        assertThat(request.getSession(false)).isNull();
    }

    @Test
    void 미가입_계정으로_재인증하면_탈퇴하지_않는다() throws Exception {
        var request = requestWithRedirectUrl(FRONTEND_BASE_URL);
        request.getSession().setAttribute("pendingWithdraw", true);
        request.getSession().setAttribute("withdrawMemberId", 1L);
        var response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication(NEW_MEMBER_ATTRIBUTES, null));

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_BASE_URL + "/login?error=reauth_mismatch");
        verify(memberService, never()).withdraw(1L);
    }

    @Test
    void 동일_회원으로_재인증해야_탈퇴를_진행한다() throws Exception {
        var request = requestWithRedirectUrl(FRONTEND_BASE_URL);
        request.getSession().setAttribute("pendingWithdraw", true);
        request.getSession().setAttribute("withdrawMemberId", 1L);
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(1L);
        var response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication(Map.of("sub", "same-user"), member));

        verify(memberService).withdraw(1L);
        assertThat(request.getSession(false)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.bombom.news.evil.example/path",
            "https://www.bombom.news@evil.example/path",
            "https://www.bombom.news:444/path",
            "https://www.bombom.news%2eevil.example/path"
    })
    void 허용_주소와_접두사만_같은_리다이렉트를_거부한다(String redirectUrl) throws Exception {
        var request = requestWithRedirectUrl(redirectUrl);
        var response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request, response, authentication(Map.of("sub", "oauth-user"), mock(Member.class)));
        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_BASE_URL + "/");
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://www.bombom.news:443/blog", "https://WWW.BOMBOM.NEWS/blog"})
    void 같은_Origin의_기본_포트와_호스트_대소문자는_허용한다(String redirectUrl) throws Exception {
        var response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(requestWithRedirectUrl(redirectUrl), response,
                authentication(Map.of("sub", "oauth-user"), mock(Member.class)));
        assertThat(response.getRedirectedUrl()).isEqualTo(redirectUrl + "/");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/allowed-bypass", "/allowed/../outside", "/allowed/%2e%2e/outside"})
    void 경로가_포함된_허용_목록의_경계를_우회할_수_없다(String path) throws Exception {
        ReflectionTestUtils.setField(handler, "redirectUriWhitelist", List.of(FRONTEND_BASE_URL + "/allowed"));
        var response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(requestWithRedirectUrl(FRONTEND_BASE_URL + path), response,
                authentication(Map.of("sub", "oauth-user"), mock(Member.class)));
        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_BASE_URL + "/");
    }

    private MockHttpServletRequest requestWithRedirectUrl(String redirectUrl) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute("redirectUrl", redirectUrl);
        return request;
    }

    private OAuth2AuthenticationToken authentication(Map<String, Object> attributes, Member member) {
        CustomOAuth2User user = new CustomOAuth2User(
                attributes,
                member,
                null,
                null
        );
        return new OAuth2AuthenticationToken(user, List.of(), "google");
    }
}
