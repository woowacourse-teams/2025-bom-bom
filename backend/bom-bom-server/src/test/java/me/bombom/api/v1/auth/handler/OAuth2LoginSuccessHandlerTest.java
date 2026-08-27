package me.bombom.api.v1.auth.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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

    @BeforeEach
    void setUp() {
        uniqueUserInfoGenerator = mock(UniqueUserInfoGenerator.class);
        handler = new OAuth2LoginSuccessHandler(
                mock(MemberService.class),
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
