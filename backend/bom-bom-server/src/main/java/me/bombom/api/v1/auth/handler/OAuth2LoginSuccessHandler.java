package me.bombom.api.v1.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String LOCAL_ENV = "local";
    private static final String SIGNUP_PATH = "/signup";
    private static final String HOME_PATH = "/";

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${frontend.local-url}")
    private String frontendLocalUrl;

    private final MemberService memberService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        HttpSession session = request.getSession();
        Boolean pendingWithdraw = (Boolean) session.getAttribute("pendingWithdraw");
        Long withdrawMemberId = (Long) session.getAttribute("withdrawMemberId");

        if (pendingWithdraw != null && pendingWithdraw && withdrawMemberId != null) {
            try {
                // 재인증 후 탈퇴 처리 (Apple 토큰 철회는 이미 AuthController에서 처리됨)
                memberService.revoke(withdrawMemberId);
                session.invalidate();
                SecurityContextHolder.clearContext();
                String redirectUrl = getBaseUrlByEnv(request);
                response.sendRedirect(redirectUrl);
                return;
            } catch (Exception e) {
                // 탈퇴 처리 실패 시에도 홈으로 리다이렉트
                log.error("재인증 후 탈퇴 처리 중 예외 발생 - memberId: {}", withdrawMemberId, e);
                session.invalidate();
                String redirectUrl = getBaseUrlByEnv(request);
                response.sendRedirect(redirectUrl);
                return;
            }
        }
        Member member = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomOAuth2User) {
            member = ((CustomOAuth2User) principal).getMember();
        }

        String redirectUrl = getBaseUrlByEnv(request);
        redirectUrl += (member == null) ? SIGNUP_PATH : HOME_PATH;
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
