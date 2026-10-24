package me.bombom.support.auth;

import java.util.Optional;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.dto.request.NativeLoginRequest;
import me.bombom.api.v1.auth.service.AppleOAuth2Service;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.support.testdouble.ResettableTestDouble;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 통합 테스트에서 실제 Apple OAuth2 API 호출 없이 인증 Bean 의존성을 대체한다.
 */
public class FakeAppleOAuth2Service extends AppleOAuth2Service implements ResettableTestDouble {

    private AppleOAuth2Service webDelegate;

    public void delegateWebLoginTo(AppleOAuth2Service service) { webDelegate = service; }

    private Member webMember;
    private Member nativeMember;
    private PendingOAuth2Member nativeSignup;

    public void allowNativeSignup(PendingOAuth2Member pending) { nativeSignup = pending; }

    public void allowNativeLogin(Member member) { nativeMember = member; }
    private boolean webLoginEnabled;

    public void allowWebLogin(Member member) {
        webMember = member;
        webLoginEnabled = true;
    }

    @Override
    public void reset() {
        webDelegate = null;
        webMember = null;
        nativeMember = null;
        nativeSignup = null;
        webLoginEnabled = false;
    }

    public FakeAppleOAuth2Service() {
        super(null, null, null, null, null, null);
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        if (webDelegate != null) return webDelegate.loadUser(userRequest);
        if (!webLoginEnabled) {
            throw new UnsupportedOperationException("통합 테스트에서는 외부 Apple 로그인을 호출하지 않는다.");
        }
        return new CustomOAuth2User(
                userRequest.getIdToken().getClaims(), webMember, userRequest.getIdToken(), null);
    }

    @Override
    public boolean revokeToken(String accessToken) {
        return true;
    }

    @Override
    public Optional<Member> loginWithNative(NativeLoginRequest request) {
        if (nativeSignup != null) {
            var attributes = (ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes();
            attributes.getRequest().getSession().setAttribute("pendingMember", nativeSignup);
        }
        return Optional.ofNullable(nativeMember);
    }
}
