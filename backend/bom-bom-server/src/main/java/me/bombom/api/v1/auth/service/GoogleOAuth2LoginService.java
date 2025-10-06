package me.bombom.api.v1.auth.service;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.dto.request.NativeLoginRequest;
import me.bombom.api.v1.auth.enums.OAuth2Provider;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Google OAuth2 로그인 서비스
 * Google의 표준 OAuth2 플로우를 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoogleOAuth2LoginService implements OAuth2LoginService {

    private final DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
    private final MemberRepository memberRepository;
    private final HttpSession session;
    private final RestClient.Builder restClientBuilder;
    private final IdTokenValidator idTokenValidator;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth2.google.app-client-id}")
    private String googleAppClientId;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("Google OAuth2 로그인 처리 시작");
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);

        String providerId = oAuth2User.getAttribute("sub");
        String profileUrl = oAuth2User.getAttribute("picture");

        // 기존 회원 확인
        Optional<Member> member = memberRepository.findByProviderAndProviderId("google", providerId);

        if (member.isEmpty()) {
            PendingOAuth2Member pendingMember = PendingOAuth2Member.builder()
                    .provider("google")
                    .providerId(providerId)
                    .profileUrl(profileUrl)
                    .build();
            session.setAttribute("pendingMember", pendingMember);
            log.info("Google 신규 회원 - 회원가입 대기 상태로 설정");
        } else {
            log.info("Google 기존 회원 - 로그인 성공, memberId: {}", member.get().getId());
        }
        return new CustomOAuth2User(oAuth2User.getAttributes(), member.orElse(null), null, null);
    }

    @Transactional
    public Optional<Member> loginWithNative(NativeLoginRequest request) {
        try {
//            Map<String, Object> tokenResponse = exchangeGoogleToken(request.authorizationCode());
//            saveGoogleAccessToken(tokenResponse);

            String sub = validateAndExtractGoogleSubject(request.identityToken());
            return findMemberAndSetPendingIfNew(sub);
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                    .addContext("reason", "google_native_exchange_failed");
        }
    }

    private java.util.Map<String, Object> exchangeGoogleToken(String authorizationCode) {
        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "authorization_code");
        body.add("code", authorizationCode);
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
        body.add("redirect_uri", "postmessage");

        java.util.Map<String, Object> tokenResponse = restClientBuilder.build().post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (tokenResponse == null) {
            throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                    .addContext("reason", "google_token_response_is_null");
        }
        if (tokenResponse.containsKey("error")) {
            Object err = tokenResponse.get("error");
            Object desc = tokenResponse.get("error_description");
            log.error("Google 토큰 교환 실패 - error: {}, description: {}", err, desc);
            throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                    .addContext("reason", String.valueOf(desc));
        }
        return tokenResponse;
    }

    private void saveGoogleAccessToken(java.util.Map<String, Object> tokenResponse) {
        Object accessToken = tokenResponse.get("access_token");
        session.setAttribute("googleAccessToken", accessToken);
        log.info("Google 네이티브 Access Token 세션에 저장 완료");
    }

    private String validateAndExtractGoogleSubject(String idToken) {
        try {
            return idTokenValidator.validateGoogleAndGetSubject(idToken, googleClientId);
        } catch (UnauthorizedException e) {
            log.info("첫 번째 client ID로 검증 실패, 두 번째 client ID로 재시도 - clientId: {}, error: {}", googleClientId, e.getMessage());
            try {
                return idTokenValidator.validateGoogleAndGetSubject(idToken, googleAppClientId);
            } catch (UnauthorizedException e2) {
                log.info("두 번째 client ID로도 검증 실패 - appClientId: {}, error: {}", googleAppClientId, e2.getMessage());
                throw e2; // 두 번 모두 실패했을 때 예외 던지기
            }
        }
    }

    private Optional<Member> findMemberAndSetPendingIfNew(String sub) {
        Optional<Member> member = memberRepository.findByProviderAndProviderId(OAuth2Provider.GOOGLE.getValue(), sub);
        if (member.isEmpty()) {
            PendingOAuth2Member pending = PendingOAuth2Member.builder()
                    .provider(OAuth2Provider.GOOGLE.getValue())
                    .providerId(sub)
                    .profileUrl(null)
                    .build();
            session.setAttribute("pendingMember", pending);
            return Optional.empty();
        }
        return member;
    }

    @Override
    public boolean supports(String provider) {
        return OAuth2Provider.GOOGLE.isEqualProvider(provider);
    }
}
