package me.bombom.api.v1.auth.controller;

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
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.dto.request.NativeLoginRequest;
import me.bombom.api.v1.auth.dto.request.SignupValidateRequest;
import me.bombom.api.v1.auth.dto.response.NativeLoginResponse;
import me.bombom.api.v1.auth.enums.OAuth2Provider;
import me.bombom.api.v1.auth.enums.SignupValidateStatus;
import me.bombom.api.v1.auth.service.AppleOAuth2Service;
import me.bombom.api.v1.auth.service.GoogleOAuth2LoginService;
import me.bombom.api.v1.auth.support.CookieManager;
import me.bombom.api.v1.auth.support.SessionManager;
import me.bombom.api.v1.auth.util.UniqueUserInfoGenerator;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.service.MemberService;
import org.springframework.http.HttpStatus;
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

    private static final String JSESSIONID = "JSESSIONID";

    private final MemberService memberService;
    private final AppleOAuth2Service appleOAuth2Service;
    private final GoogleOAuth2LoginService googleOAuth2LoginService;
    private final UniqueUserInfoGenerator uniqueUserInfoGenerator;
    private final SessionManager sessionManager;
    private final CookieManager cookieManager;

    @Override
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@Valid @RequestBody MemberSignupRequest signupRequest, HttpServletRequest request) {
        Optional<HttpSession> optionalSession = sessionManager.get(request);
        if (optionalSession.isEmpty()) {
            throw new UnauthorizedException(ErrorDetail.MISSING_OAUTH_DATA)
                .addContext("sessionExists", false)
                .addContext("requestedEmail", signupRequest.email());
        }
        PendingOAuth2Member pendingMember = (PendingOAuth2Member) sessionManager.getAttribute(request, "pendingMember");
        log.info("회원가입 요청 - sessionId: {}, pendingMember: {}", optionalSession.get().getId(), pendingMember);
        if (pendingMember == null) {
            throw new UnauthorizedException(ErrorDetail.MISSING_OAUTH_DATA)
                .addContext("sessionExists", true)
                .addContext("pendingMemberExists", false)
                .addContext("requestedEmail", signupRequest.email());
        }
        Member newMember = memberService.signup(pendingMember, signupRequest);
        sessionManager.removeAttribute(request, "pendingMember");

        // 회원가입 후 로그인 처리 - 세션에 인증 정보 저장
        OAuth2AuthenticationToken authentication = createAuthenticationToken(newMember);
        sessionManager.setAuth(request, authentication);
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
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        sessionManager.setAttribute(request, "env", env);
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    // 통합 네이티브 엔드포인트: /login/{provider}/native
    @Override
    @PostMapping("/login/{provider}/native")
    public NativeLoginResponse nativeLogin(
            @PathVariable("provider") String provider,
            @Valid @RequestBody NativeLoginRequest nativeLoginRequest,
            HttpServletRequest request
    ) {
        Optional<Member> loginResult = loginWithProvider(provider, nativeLoginRequest);
        return handleNativeResult(nativeLoginRequest, loginResult, request);
    }

    @Override
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        sessionManager.clearAuth(request);
        cookieManager.delete(response, JSESSIONID);
    }

    @Override
    @PostMapping("/withdraw")
    public void withdraw(
            @LoginMember Member member,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String appleAccessToken = (String) sessionManager.getAttribute(request, "appleAccessToken");
        // Apple 로그인 사용자이고 Access Token이 없는 경우
        if (member.getProvider().equals("apple") && appleAccessToken == null) {
            // 탈퇴 플래그 저장 후 재로그인 요구
            sessionManager.setAttribute(request, "pendingWithdraw", true);
            sessionManager.setAttribute(request, "withdrawMemberId", member.getId());
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
        sessionManager.clearAuth(request);
        cookieManager.delete(response, JSESSIONID);
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
        sessionManager.ensure(request);

        if (member.isPresent()) {
            OAuth2AuthenticationToken authentication = createAuthenticationToken(member.get());
            sessionManager.setAuth(request, authentication);

            // 기존 회원 -> 로그인 완료
            return new NativeLoginResponse(true, null, null);
        } else {
            // 신규 회원 -> 회원가입 필요
            String uniqueEmailLocalPart = uniqueUserInfoGenerator.getUniqueEmailLocalPart(nativeLoginRequest.email());
            String uniqueNickname = uniqueUserInfoGenerator.getUniqueNickname(nativeLoginRequest.nickname());
            return new NativeLoginResponse(false, uniqueEmailLocalPart, uniqueNickname);
        }
    }

    //TODO: 이거도 분리하자
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
