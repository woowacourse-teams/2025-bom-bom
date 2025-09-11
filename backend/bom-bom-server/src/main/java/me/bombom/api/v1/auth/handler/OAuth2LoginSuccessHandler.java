package me.bombom.api.v1.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.service.AppleOAuth2Service;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

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

    private final AppleOAuth2Service appleOAuth2Service;
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
            // 탈퇴 대기 중이었다면 자동으로 탈퇴 진행
            String appleAccessToken = (String) session.getAttribute("appleAccessToken");
            
            try {
                // 회원 정보 조회 (Member 엔티티 직접 조회)
                Member member = memberService.findById(withdrawMemberId);
                
                // Apple 로그인 사용자인 경우 Token Revoke 처리
                if (member.getProvider().equals("apple")) {
                    appleOAuth2Service.processWithdrawal(member, appleAccessToken);
                }
                
                // 회원 데이터 소프트 삭제
                memberService.revoke(withdrawMemberId);
                session.invalidate();
                
                // 탈퇴 완료 후 메인 페이지로 리다이렉트
                String redirectUrl = getBaseUrlByEnv(request) + "?withdraw=success";
                response.sendRedirect(redirectUrl);
                return;
            } catch (Exception e) {
                // 탈퇴 처리 실패 시 에러 페이지로 리다이렉트
                String redirectUrl = getBaseUrlByEnv(request) + "?withdraw=error";
                response.sendRedirect(redirectUrl);
                return;
            }
        }

        // 일반 로그인 성공 처리
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Member member = oAuth2User.getMember();

        String redirectUrl = getBaseUrlByEnv(request);
        if (member == null) {
            redirectUrl += SIGNUP_PATH;
        } else {
            redirectUrl += HOME_PATH;
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
