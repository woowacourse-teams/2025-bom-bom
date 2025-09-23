package me.bombom.api.v1.auth.service;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.AppleClientSecretSupplier;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.dto.request.NativeLoginRequest;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Apple OAuth2 통합 서비스
 * Apple 로그인, Token Revoke, 탈퇴 처리를 모두 담당합니다
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppleOAuth2Service extends OidcUserService {

    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke";
    
    private final MemberRepository memberRepository;
    private final HttpSession session;
    private final RestClient.Builder restClientBuilder;
    private final AppleClientSecretSupplier appleClientSecretSupplier;
    private final IdTokenValidator idTokenValidator;

    //웹 로그인에서 사용
    @Value("${oauth2.apple.client-id}")
    private String clientId;

    //앱 로그인에서 사용
    @Value("${oauth2.apple.bundle-id:}")
    private String bundleId;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("Apple OIDC 로그인 처리 시작");
        
        try {
            // 기본 OidcUser 로드
            OidcUser oidcUser = super.loadUser(userRequest);
            
            String providerId = oidcUser.getSubject();
            log.info("Apple OIDC 사용자 정보 - providerId: {}", providerId);
            
            // Apple Access Token 추출 및 세션에 저장
            String accessToken = extractAccessTokenFromOidcRequest(userRequest);
            if (accessToken != null) {
                session.setAttribute("appleAccessToken", accessToken);
                session.setAttribute("appleClientId", clientId);
                log.info("Apple Access Token 세션에 저장 완료");
            } else {
                log.warn("Apple Access Token 추출 실패");
            }
            
            // user 추가 정보(name 등) 파싱 (form_post로 전달되는 user JSON)
            try {
                Object userParam = userRequest.getAdditionalParameters().get("user");
                if (userParam instanceof String userJson && !userJson.isBlank()) {
                    // attributes에 병합하여 이후 Extractor에서 접근 가능하도록 함
                    Map<String, Object> merged = new HashMap<>(oidcUser.getAttributes());
                    merged.put("user", userJson);
                    oidcUser = new org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser(
                            oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo(), "sub");
                }
            } catch (Exception ignore) {
                // user 파라미터가 없거나 파싱 실패해도 치명적이지 않음
            }

            // 기존 회원 확인
            Optional<Member> member = findMemberAndSetPendingIfNew(providerId);
            return new CustomOAuth2User(oidcUser.getAttributes(), member.orElse(null), oidcUser.getIdToken(), oidcUser.getUserInfo());
        } catch (Exception e) {
            log.error("Apple OIDC 로그인 처리 실패 - error: {}", e.getMessage(), e);
            throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                .addContext("provider", "apple")
                .addContext("reason", "apple_oidc_processing_failed");
        }
    }

    /**
     * Apple Access Token을 철회합니다
     * @param accessToken 철회할 Access Token
     * @return 철회 성공 여부
     */
    public boolean revokeToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("철회할 Apple Access Token이 없습니다.");
            return false;
        }
        try {
            String revokeClientId = (String) session.getAttribute("appleClientId");
            if (revokeClientId == null || revokeClientId.isBlank()) {
                revokeClientId = clientId;
            }
            log.info("Apple Token Revoke 시작 - clientId: {}", revokeClientId);

            MultiValueMap<String, String> requestBody = buildRevokeRequestBody(accessToken, revokeClientId);

            restClientBuilder.build().post()
                .uri(APPLE_REVOKE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();

            log.info("Apple Token Revoke 성공");
            return true;

        } catch (Exception e) {
            log.warn("Apple Token Revoke 실패 - error: {}", e.getMessage());
            return false;
        }
    }

    /**
     *  iOS 네이티브 로그인 처리: 번들 ID로 client_secret 생성하여 코드 교환
     */
    public Optional<Member> loginWithNative(NativeLoginRequest request) {
        try {
            if (bundleId == null || bundleId.isBlank()) {
                throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                        .addContext("reason", "bundleId_not_configured");
            }

            String subject = idTokenValidator.validateAppleAndGetSubject(request.identityToken(), bundleId);
            Map<String, Object> token = requestAppleToken(request.authorizationCode(), bundleId);

            session.setAttribute("appleAccessToken", token.get("access_token"));
            session.setAttribute("appleClientId", bundleId);
            return findMemberAndSetPendingIfNew(subject);
        } catch (UnauthorizedException e) {
            log.error("Apple 네이티브 로그인 실패 - UnauthorizedException: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Apple 네이티브 로그인 실패 - Exception: {}", e.getMessage(), e);
            throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                    .addContext("reason", "apple_native_exchange_failed")
                    .addContext("error_detail", e.getMessage());
        }
    }

    private MultiValueMap<String, String> buildRevokeRequestBody(String accessToken, String revokeClientId) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("token", accessToken);
        requestBody.add("client_id", revokeClientId);
        String revokeClientSecret = revokeClientId.equals(this.clientId) ? appleClientSecretSupplier.get() : appleClientSecretSupplier.generateFor(
                revokeClientId);
        requestBody.add("client_secret", revokeClientSecret);
        requestBody.add("token_type_hint", "access_token");
        return requestBody;
    }

    /**
     * Apple Access Token을 추출합니다 (OidcUserRequest용)
     * @param userRequest OIDC 사용자 요청
     * @return Access Token 또는 null
     */
    private String extractAccessTokenFromOidcRequest(OidcUserRequest userRequest) {
        try {
            Object accessTokenObj = userRequest.getAdditionalParameters().get(ACCESS_TOKEN_KEY);
            if (accessTokenObj != null) {
                log.info("Apple Access Token 추출 성공");
                return accessTokenObj.toString();
            }
            return null;
        } catch (Exception e) {
            log.warn("Apple Access Token 추출 실패 - error: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> requestAppleToken(String code, String clientIdForExchange) {
        MultiValueMap<String, String> body = buildAccessTokenRequestBody(code, clientIdForExchange);
        Map<String, Object> responseMap = restClientBuilder.build().post()
                .uri("https://appleid.apple.com/auth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (responseMap == null) {
            throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                    .addContext("reason", "apple_token_response_is_null");
        }
        if (responseMap.containsKey("error")) {
            log.error("Apple 토큰 교환 실패 - error: {}, description: {}", responseMap.get("error"), responseMap.get("error_description"));
            throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                    .addContext("reason", responseMap.get("error_description"));
        }
        return responseMap;
    }

    private MultiValueMap<String, String> buildAccessTokenRequestBody(String code, String clientIdForExchange) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", clientIdForExchange);
        String clientSecret = clientIdForExchange != null && !clientIdForExchange.equals(this.clientId)
                ? appleClientSecretSupplier.generateFor(clientIdForExchange)
                : appleClientSecretSupplier.get();
        body.add("client_secret", clientSecret);
        return body;
    }

    // 공통: 기존 회원 조회 + 신규면 pendingMember 세션 저장
    private Optional<Member> findMemberAndSetPendingIfNew(String providerId) {
        Optional<Member> member = memberRepository.findByProviderAndProviderId("apple", providerId);
        if (member.isEmpty()) {
            PendingOAuth2Member pendingMember = PendingOAuth2Member.builder()
                    .provider("apple")
                    .providerId(providerId)
                    .profileUrl(null)
                    .build();
            session.setAttribute("pendingMember", pendingMember);
            log.info("Apple 신규 사용자 - 회원가입 대기 상태로 설정, providerId: {}", providerId);
        } else {
            log.info("Apple 기존 사용자 - memberId: {}", member.get().getId());
        }
        return member;
    }
}
