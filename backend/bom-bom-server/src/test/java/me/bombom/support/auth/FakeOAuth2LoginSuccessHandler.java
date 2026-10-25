package me.bombom.support.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import me.bombom.support.testdouble.ResettableTestDouble;
import org.springframework.security.core.Authentication;

/**
 * 기본적으로 성공 후 처리를 생략하고, 필요한 회귀 검사에서는 실제 핸들러에 위임한다.
 */
public final class FakeOAuth2LoginSuccessHandler extends OAuth2LoginSuccessHandler implements ResettableTestDouble {

    private OAuth2LoginSuccessHandler delegate;

    public void delegateTo(OAuth2LoginSuccessHandler handler) { delegate = handler; }

    @Override
    public void reset() { delegate = null; }

    public FakeOAuth2LoginSuccessHandler() {
        super(null, null);
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        if (delegate != null) {
            delegate.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        // 테스트에서는 OAuth2 로그인 성공 이후의 실제 리다이렉트와 토큰 발급 흐름을 수행하지 않는다.
    }
}
