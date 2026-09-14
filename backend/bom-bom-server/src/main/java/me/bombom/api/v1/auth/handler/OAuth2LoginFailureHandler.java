package me.bombom.api.v1.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.diagnostic.OAuth2Diagnostics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * OAuth2 로그인 실패 핸들러
 * 로그인 실패 시 www.bombom.news/login?error로 리다이렉트
 */
@RequiredArgsConstructor
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final OAuth2Diagnostics diagnostics;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        diagnostics.failed(request, exception);

        String redirectUrl = frontendBaseUrl + "/login?error";
        response.sendRedirect(redirectUrl);
    }
}
