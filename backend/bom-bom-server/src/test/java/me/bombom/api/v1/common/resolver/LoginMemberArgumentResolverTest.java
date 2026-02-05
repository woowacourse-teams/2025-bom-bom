package me.bombom.api.v1.common.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

class LoginMemberArgumentResolverTest {

    private MemberRepository memberRepository;
    private LoginMemberArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        memberRepository = Mockito.mock(MemberRepository.class);
        resolver = new LoginMemberArgumentResolver(memberRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    //테스트를 위한 가짜 컨트롤러
    static class StubController {
        public void endpointNullableTrue(@LoginMember(anonymous = true) Member member) {
        }

        public void endpointNullableFalse(@LoginMember(anonymous = false) Member member) {
        }

        public void endpointLong(@LoginMember Long memberId) {
        }

        public void endpointLongAnonymousTrue(@LoginMember(anonymous = true) Long memberId) {
        }
    }

    @Test
    void supportsParameter는_Member와_Long타입을_지원한다() throws Exception {
        assertThat(resolver.supportsParameter(paramNullableTrue())).isTrue();
        assertThat(resolver.supportsParameter(paramLongAnonymousFalse())).isTrue();
    }

    @Test
    void 익명_요청_anonymous_true이면_null_반환() throws Exception {
        // 익명 토큰 = 로그인하지 않은 유저
        Authentication anonymous = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        Object result = resolver.resolveArgument(paramNullableTrue(), null, null, null);
        assertThat(result).isNull();
    }

    @Test
    void 익명_요청_anonymous_false이면_UNAUTHORIZED() {
        Authentication anonymous = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        assertThatThrownBy(() -> resolver.resolveArgument(paramNullableFalse(), null, null, null))
                .isInstanceOfSatisfying(UnauthorizedException.class, e ->
                        assertThat(e.getErrorDetail()).isEqualTo(ErrorDetail.UNAUTHORIZED)
                );
    }

    @Test
    void Long타입_익명_요청_anonymous_true이면_null_반환() throws Exception {
        Authentication anonymous = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        Object result = resolver.resolveArgument(paramLongAnonymousTrue(), null, null, null);
        assertThat(result).isNull();
    }

    @Test
    void Long타입_익명_요청_anonymous_false이면_UNAUTHORIZED() {
        Authentication anonymous = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        assertThatThrownBy(() -> resolver.resolveArgument(paramLongAnonymousFalse(), null, null, null))
                .isInstanceOfSatisfying(UnauthorizedException.class, e ->
                        assertThat(e.getErrorDetail()).isEqualTo(ErrorDetail.UNAUTHORIZED)
                );
    }

    @Test
    void principal_타입_불일치면_INVALID_TOKEN() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("notCustomUser", null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatThrownBy(() -> resolver.resolveArgument(paramNullableTrue(), null, null, null))
                .isInstanceOfSatisfying(UnauthorizedException.class, e ->
                        assertThat(e.getErrorDetail()).isEqualTo(ErrorDetail.INVALID_TOKEN)
                );
    }

    @Test
    void principal은_CustomOAuth2User지만_member_null이면_UNAUTHORIZED() {
        CustomOAuth2User user = new CustomOAuth2User(Map.of("name", "tester"), null, null, null);
        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                user,
                user.getAuthorities(),
                "registrationId"
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatThrownBy(() -> resolver.resolveArgument(paramNullableTrue(), null, null, null))
                .isInstanceOfSatisfying(UnauthorizedException.class, e ->
                        assertThat(e.getErrorDetail()).isEqualTo(ErrorDetail.UNAUTHORIZED)
                );
    }

    @Test
    void 정상_로그인_이면_Member_반환() throws Exception {
        // Member는 복잡한 엔티티일 수 있으므로 목 객체로 대체
        Member member = mock(Member.class);

        CustomOAuth2User user = new CustomOAuth2User(Map.of("name", "tester"), member, null, null);
        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                user,
                user.getAuthorities(),
                "registrationId"
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        given(memberRepository.findById(1L)).willReturn(java.util.Optional.of(member));

        // when
        Object result = resolver.resolveArgument(paramNullableTrue(), null, null, null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isInstanceOf(Member.class);
            softly.assertThat(result).isEqualTo(member);
        });
    }

    @Test
    void 정상_로그인이지만_DB에_회원이_없으면_UNAUTHORIZED() throws Exception {
        // given
        Member member = org.mockito.Mockito.mock(Member.class);
        given(member.getId()).willReturn(0L);

        CustomOAuth2User user = new CustomOAuth2User(Map.of("name", "tester"), member, null, null);
        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                user,
                user.getAuthorities(),
                "registrationId"
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        given(memberRepository.findById(0L)).willReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(paramNullableTrue(), null, null, null))
                .isInstanceOfSatisfying(UnauthorizedException.class,
                        e -> assertThat(e.getErrorDetail()).isEqualTo(ErrorDetail.UNAUTHORIZED));
    }

    @Test
    void Long타입_요청이면_memberId_반환() throws Exception {
        Member member = mock(Member.class);
        Long memberId = 1L;
        when(member.getId()).thenReturn(memberId);

        CustomOAuth2User user = new CustomOAuth2User(Map.of("name", "tester"), member, null, null);
        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                user,
                user.getAuthorities(),
                "registrationId"
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Object result = resolver.resolveArgument(paramLongAnonymousFalse(), null, null, null);
        assertThat(result).isEqualTo(memberId);
    }

    // ArgumentResolver에게 파라미터 넘기기
    private MethodParameter paramNullableTrue() throws Exception {
        Method m = StubController.class.getMethod("endpointNullableTrue", Member.class);
        return new MethodParameter(m, 0);
    }

    private MethodParameter paramNullableFalse() throws Exception {
        Method m = StubController.class.getMethod("endpointNullableFalse", Member.class);
        return new MethodParameter(m, 0);
    }

    private MethodParameter paramLongAnonymousFalse() throws Exception {
        Method m = StubController.class.getMethod("endpointLong", Long.class);
        return new MethodParameter(m, 0);
    }

    private MethodParameter paramLongAnonymousTrue() throws Exception {
        Method m = StubController.class.getMethod("endpointLongAnonymousTrue", Long.class);
        return new MethodParameter(m, 0);
    }
}
