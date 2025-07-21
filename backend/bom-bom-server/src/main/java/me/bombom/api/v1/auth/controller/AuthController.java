    package me.bombom.api.v1.auth.controller;

    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import jakarta.servlet.http.HttpSession;
    import java.io.IOException;
    import lombok.RequiredArgsConstructor;
    import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
    import me.bombom.api.v1.common.exception.UnauthorizedException;
    import me.bombom.api.v1.common.exception.ErrorDetail;
    import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
    import me.bombom.api.v1.member.service.MemberService;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/api/v1/auth")
    public class AuthController {

        private final MemberService memberService;

        @PostMapping("/signup")
        public void signup(@RequestBody MemberSignupRequest signupRequest, HttpServletRequest request) {
            HttpSession session = request.getSession();
            PendingOAuth2Member pendingMember = (PendingOAuth2Member) session.getAttribute("pendingMember");
            if (pendingMember == null) {
                throw new UnauthorizedException(ErrorDetail.MISSING_OAUTH_DATA);
            }
            memberService.signup(pendingMember, signupRequest);
            session.removeAttribute("pendingMember");
        }

        @GetMapping("/login/{provider}")
        public void login(@PathVariable("provider") String provider, HttpServletResponse response) throws IOException {
            response.sendRedirect("/oauth2/authorization/" + provider);
        }

        @PostMapping("/logout")
        public void logout(HttpServletRequest request) {
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
        }
    }
