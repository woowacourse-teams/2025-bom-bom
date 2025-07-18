//package me.bombom.api.v1.auth.handler;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import me.bombom.api.v1.auth.dto.CustomOAuth2User;
//import me.bombom.api.v1.member.domain.Member;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//
//public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
//
//    @Override
//    public void onAuthenticationSuccess(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            Authentication authentication
//    ) throws IOException, ServletException {
//        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
//        Member member = (Member) oAuth2User.getMember();
//
//        if (member == null) {
//            // 추가 정보 입력
//            response.sendRedirect("/api/v1/auth/signup");
//        } else {
//            // 로그인 성공
//            response.sendRedirect("/");
//        }
//    }
//}
