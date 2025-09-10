package me.bombom.api.v1.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String LOCAL_ENV = "local";
    private static final String SIGNUP_PATH = "/signup";
    private static final String HOME_PATH = "/";

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
        System.out.println("=== OAuth2LoginSuccessHandler 호출됨 ===");
        System.out.println("authentication: " + authentication.getClass().getSimpleName());
        
        Object principal = authentication.getPrincipal();
        System.out.println("principal type: " + principal.getClass().getSimpleName());
        
        Member member = null;
        
        if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) principal;
            member = oAuth2User.getMember();
            System.out.println("CustomOAuth2User - member: " + (member != null ? "있음 (ID: " + member.getId() + ")" : "없음"));
        } else if (principal instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) principal;
            System.out.println("OidcUser - sub: " + oidcUser.getSubject());
            System.out.println("OidcUser - email: " + oidcUser.getEmail());
            System.out.println("OidcUser - name: " + oidcUser.getFullName());
            // Apple OIDC의 경우 CustomOAuth2UserService가 호출되지 않았을 수 있음
            System.out.println("Apple OIDC 로그인 - CustomOAuth2UserService 호출되지 않음");
        } else if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            System.out.println("OAuth2User - name: " + oAuth2User.getName());
            System.out.println("OAuth2User - attributes: " + oAuth2User.getAttributes());
        }

        String redirectUrl = getBaseUrlByEnv(request);
        if (member == null) {
            redirectUrl +=  SIGNUP_PATH;
        } else {
            redirectUrl +=  HOME_PATH;
        }

        response.sendRedirect(redirectUrl);
    }

    private String getBaseUrlByEnv(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String env = session != null ? (String) session.getAttribute("env") : null;
        if (LOCAL_ENV.equals(env)) {
            return frontendLocalUrl;
        }
        return frontendBaseUrl;
    }
}
