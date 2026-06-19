package me.bombom.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import org.springframework.security.core.Authentication;

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
