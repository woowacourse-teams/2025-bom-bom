package me.bombom.api.v1.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${frontend.local-url}")
    private String frontendLocalUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Member member = oAuth2User.getMember();

        // TODO: 운영 환경 사용 전에 제거
        String frontendRedirectUrl = frontendBaseUrl;
        String state = request.getParameter("state");
        if (state != null && state.contains("env=local")) {
            frontendRedirectUrl = frontendLocalUrl;
        }

        if (member == null) {
            response.sendRedirect(frontendRedirectUrl + "/signup");
        } else {
            response.sendRedirect(frontendRedirectUrl + "/");
        }
    }
}
