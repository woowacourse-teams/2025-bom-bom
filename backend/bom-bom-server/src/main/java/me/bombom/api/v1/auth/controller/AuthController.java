package me.bombom.api.v1.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.dto.request.DuplicateCheckRequest;
import me.bombom.api.v1.auth.service.AppleOAuth2Service;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerApi{

    private final MemberService memberService;
    private final AppleOAuth2Service appleOAuth2Service;

    @Override
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@Valid @RequestBody MemberSignupRequest signupRequest, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new UnauthorizedException(ErrorDetail.MISSING_OAUTH_DATA)
                .addContext("sessionExists", false)
                .addContext("requestedEmail", signupRequest.email());
        }
        PendingOAuth2Member pendingMember = (PendingOAuth2Member) session.getAttribute("pendingMember");
        if (pendingMember == null) {
            throw new UnauthorizedException(ErrorDetail.MISSING_OAUTH_DATA)
                .addContext("sessionExists", true)
                .addContext("pendingMemberExists", false)
                .addContext("requestedEmail", signupRequest.email());
        }
        Member newMember = memberService.signup(pendingMember, signupRequest);
        session.removeAttribute("pendingMember");

        OAuth2AuthenticationToken authentication = createAuthenticationToken(newMember);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    @GetMapping("/signup/check")
    public boolean checkSignupDuplicate(@Valid @ModelAttribute DuplicateCheckRequest request) {
        return memberService.checkSignupDuplicate(request.field(), request.userInput());
    }

    @Override
    @GetMapping("/login/{provider}")
    public void login(
            @PathVariable("provider") String provider,
            @RequestParam(defaultValue = "deploy") String env,
            HttpServletResponse response,
            HttpSession httpSession
    ) throws IOException {
        httpSession.setAttribute("env", env);
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    @Override
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
    }

    @Override
    @PostMapping("/withdraw")
    public void withdraw(@LoginMember Member member, HttpSession session, HttpServletResponse response) throws IOException {
        String appleAccessToken = (String) session.getAttribute("appleAccessToken");
        
        // Apple 로그인 사용자이고 Access Token이 없는 경우
        if (member.getProvider().equals("apple") && appleAccessToken == null) {
            log.info("Apple Access Token 없음 - memberId: {}, 세션ID: {}", member.getId(), session.getId());
            
            // 탈퇴 플래그 저장 후 재로그인 요구
            session.setAttribute("pendingWithdraw", true);
            session.setAttribute("withdrawMemberId", member.getId());
            response.sendRedirect("/oauth2/authorization/apple");
            return;
        }
        log.info("회원 탈퇴 진행 - memberId: {}, provider: {}", member.getId(), member.getProvider());
        memberService.revoke(member.getId(), appleAccessToken);
        session.invalidate();
        response.sendRedirect("/");
    }

    private OAuth2AuthenticationToken createAuthenticationToken(Member member) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", member.getNickname());
        attributes.put("email", member.getEmail());
        attributes.put("profileImageUrl", member.getProfileImageUrl());
        attributes.put("provider", member.getProvider());
        attributes.put("providerId", member.getProviderId());

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, member);
        return new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                member.getProvider().toLowerCase()
        );
    }
}
