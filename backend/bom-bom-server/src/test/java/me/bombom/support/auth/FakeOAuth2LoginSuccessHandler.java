package me.bombom.support.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import org.springframework.security.core.Authentication;

/**
 * 통합 테스트에서 OAuth2 로그인 성공 후 실제 리다이렉트/토큰 발급 흐름을 비활성화한다.
 */
public final class FakeOAuth2LoginSuccessHandler extends OAuth2LoginSuccessHandler {

    public FakeOAuth2LoginSuccessHandler() {
        super(null, null);
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        // HTTP API 통합 테스트에서는 SecurityMockMvcRequestPostProcessors로 인증을 주입한다.
    }
}
