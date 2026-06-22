package me.bombom.support.auth;

import java.util.Optional;
import me.bombom.api.v1.auth.dto.request.NativeLoginRequest;
import me.bombom.api.v1.auth.service.AppleOAuth2Service;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * 통합 테스트에서 실제 Apple OAuth2 API 호출 없이 인증 Bean 의존성을 대체한다.
 */
public class FakeAppleOAuth2Service extends AppleOAuth2Service {

    public FakeAppleOAuth2Service() {
        super(null, null, null, null, null, null);
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        throw new UnsupportedOperationException("통합 테스트에서는 외부 Apple 로그인을 호출하지 않는다.");
    }

    @Override
    public boolean revokeToken(String accessToken) {
        return true;
    }

    @Override
    public Optional<Member> loginWithNative(NativeLoginRequest request) {
        return Optional.empty();
    }
}
