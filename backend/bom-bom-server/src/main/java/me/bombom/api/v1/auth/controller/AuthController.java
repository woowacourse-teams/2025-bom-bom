package me.bombom.api.v1.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.NativeLoginResponse;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.dto.request.NativeLoginRequest;
import me.bombom.api.v1.auth.dto.request.SignupValidateRequest;
import me.bombom.api.v1.auth.enums.OAuth2Provider;
import me.bombom.api.v1.auth.enums.SignupValidateStatus;
import me.bombom.api.v1.auth.service.AppleOAuth2Service;
import me.bombom.api.v1.auth.service.GoogleOAuth2LoginService;
import me.bombom.api.v1.auth.util.UniqueUserInfoGenerator;
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
    private final GoogleOAuth2LoginService googleOAuth2LoginService;
    private final UniqueUserInfoGenerator uniqueUserInfoGenerator;

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
        log.info("회원가입 요청 - sessionId: {}, pendingMember: {}", session.getId(), pendingMember);
        if (pendingMember == null) {
            throw new UnauthorizedException(ErrorDetail.MISSING_OAUTH_DATA)
                .addContext("sessionExists", true)
                .addContext("pendingMemberExists", false)
                .addContext("requestedEmail", signupRequest.email());
        }
        Member newMember = memberService.signup(pendingMember, signupRequest);
        session.removeAttribute("pendingMember");

        // 회원가입 후 로그인 처리 - 세션에 인증 정보 저장
        OAuth2AuthenticationToken authentication = createAuthenticationToken(newMember);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 세션에 인증 정보 저장 (다음 요청에서도 로그인 상태 유지)
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
    }

    @Override
    @GetMapping("/signup/check")
    public SignupValidateStatus validateSignupField(@Valid @ModelAttribute SignupValidateRequest request) {
        return memberService.validateSignupField(request.field(), request.userInput());
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

    // 통합 네이티브 엔드포인트: /login/{provider}/native
    @Override
    @PostMapping("/login/{provider}/native")
    public NativeLoginResponse nativeLogin(
            @PathVariable("provider") String provider,
            @Valid @RequestBody(required = false) NativeLoginRequest nativeLoginRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        if (nativeLoginRequest == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "응답 바디가 없습니다.");
            return null;
        }

        Optional<Member> loginResult = loginWithProvider(provider, nativeLoginRequest);
        if (loginResult.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원하지 않는 제공자입니다.");
            return null;
        }
        return handleNativeResult(nativeLoginRequest, loginResult, request);
    }

    @Override
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
            SecurityContextHolder.clearContext();
            expireSessionCookie(response);
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

        // Apple 연동 회원인 경우 토큰 철회 로직 호출
        if ("apple".equals(member.getProvider())) {
            log.info("Apple 연동 회원 탈퇴 - 토큰 철회를 시도합니다. memberId: {}", member.getId());
            boolean revokeSuccess = appleOAuth2Service.revokeToken(appleAccessToken);
            if (revokeSuccess) {
                log.info("Apple Token Revoke 성공 - memberId: {}", member.getId());
            } else {
                log.warn("Apple Token Revoke 실패 - memberId: {}, 탈퇴는 계속 진행됩니다", member.getId());
            }
        }

        memberService.withdraw(member.getId());
        session.invalidate();
        SecurityContextHolder.clearContext();
        expireSessionCookie(response);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private Optional<Member> loginWithProvider(String provider, NativeLoginRequest nativeLoginRequest) {
        if (OAuth2Provider.APPLE.isEqualProvider(provider)) {
            return appleOAuth2Service.loginWithNative(nativeLoginRequest);
        } else if (OAuth2Provider.GOOGLE.isEqualProvider(provider)) {
            return googleOAuth2LoginService.loginWithNative(nativeLoginRequest);
        }
        return Optional.empty(); // 미지원 provider
    }

    private NativeLoginResponse handleNativeResult(
            NativeLoginRequest nativeLoginRequest,
            Optional<Member> member,
            HttpServletRequest request
    ) {
        // 세션 생성 트리거 (컨테이너가 Set-Cookie: JSESSIONID를 설정)
        request.getSession(true);

        if (member.isPresent()) {
            OAuth2AuthenticationToken authentication = createAuthenticationToken(member.get());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            HttpSession session = request.getSession();
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // 기존 회원 -> 로그인 완료
            return new NativeLoginResponse(true, null, null);
        } else {
            // 신규 회원 -> 회원가입 필요
            String uniqueEmailLocalPart = uniqueUserInfoGenerator.getUniqueEmailLocalPart(nativeLoginRequest.email());
            String uniqueNickname = uniqueUserInfoGenerator.getUniqueNickname(nativeLoginRequest.nickname());
            return new NativeLoginResponse(false, uniqueEmailLocalPart, uniqueNickname);
        }
    }

    private void expireSessionCookie(HttpServletResponse response) {
        Cookie jsid = new Cookie("JSESSIONID", "");
        jsid.setMaxAge(0);
        jsid.setPath("/");
        jsid.setHttpOnly(true);
        jsid.setSecure(true);
        response.addCookie(jsid);
        // Ensure SameSite=None for cross-site
        response.addHeader("Set-Cookie", "JSESSIONID=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=None");
    }

    private OAuth2AuthenticationToken createAuthenticationToken(Member member) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", member.getNickname());
        attributes.put("email", member.getEmail());
        attributes.put("profileImageUrl", member.getProfileImageUrl());
        attributes.put("provider", member.getProvider());
        attributes.put("providerId", member.getProviderId());

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(attributes, member, null, null);
        return new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                member.getProvider().toLowerCase()
        );
    }
}
