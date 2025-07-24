package me.bombom.api.v1.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final MemberService memberService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@RequestBody MemberSignupRequest signupRequest, HttpServletRequest request) {
        log.info("========== Signup Start ==========");
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                .forEach(cookie -> log.info("요청 쿠키: {}={}", cookie.getName(), cookie.getValue()));
        } else {
            log.warn("요청에 쿠키가 전혀 없습니다.");
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            log.error("세션을 찾을 수 없습니다. request.getSession(false)가 null을 반환했습니다.");
            throw new UnauthorizedException(ErrorDetail.MISSING_OAUTH_DATA);
        }

        log.info("세션 ID: {}", session.getId());
        PendingOAuth2Member pendingMember = (PendingOAuth2Member) session.getAttribute("pendingMember");
        if (pendingMember == null) {
            log.error("세션에서 'pendingMember'를 찾을 수 없습니다.");
            throw new UnauthorizedException(ErrorDetail.MISSING_OAUTH_DATA);
        }

        log.info("성공적으로 pendingMember를 찾았습니다: {}", pendingMember);
        memberService.signup(pendingMember, signupRequest);
        session.removeAttribute("pendingMember");
        log.info("========== Signup End ==========");
    }

    @GetMapping("/login/{provider}")
    public void login(@PathVariable("provider") String provider, HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
    }
}
