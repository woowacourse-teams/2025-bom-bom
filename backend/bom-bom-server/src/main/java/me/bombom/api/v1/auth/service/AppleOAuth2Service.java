package me.bombom.api.v1.auth.service;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
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
    
    private static final String ID_TOKEN_KEY = "id_token";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke";
    
    private final MemberRepository memberRepository;
    private final HttpSession session;
    private final RestClient.Builder restClientBuilder;
    private final Supplier<String> appleClientSecretSupplier;
    
    @Value("${oauth2.apple.client-id}")
    private String clientId;

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
                log.info("Apple Access Token 세션에 저장 완료");
            } else {
                log.warn("Apple Access Token 추출 실패");
            }
            
            // 기존 회원 확인
            Optional<Member> member = memberRepository.findByProviderAndProviderId("apple", providerId);
            if (member.isEmpty()) {
                // Apple OIDC 신규 사용자 - PendingOAuth2Member를 세션에 저장
                PendingOAuth2Member pendingMember = PendingOAuth2Member.builder()
                        .provider("apple")
                        .providerId(providerId)
                        .profileUrl(null) // Apple은 profileUrl이 없음
                        .build();
                session.setAttribute("pendingMember", pendingMember);
                log.info("Apple OIDC 신규 사용자 - 회원가입 대기 상태로 설정, providerId: {}", providerId);
                log.info("세션에 pendingMember 저장 완료 - sessionId: {}, pendingMember: {}", session.getId(), pendingMember);
                return new CustomOAuth2User(oidcUser.getAttributes(), null, oidcUser.getIdToken(), oidcUser.getUserInfo());
            }
            
            log.info("Apple OIDC 기존 사용자 - memberId: {}", member.get().getId());
            return new CustomOAuth2User(oidcUser.getAttributes(), member.get(), oidcUser.getIdToken(), oidcUser.getUserInfo());
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
            log.info("Apple Token Revoke 시작 - clientId: {}", clientId);

            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("token", accessToken);
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", appleClientSecretSupplier.get());
            requestBody.add("token_type_hint", "access_token");

            restClientBuilder.build().post()
                .uri(APPLE_REVOKE_URL)
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

    /**
     * Apple Access Token을 추출합니다 (OAuth2UserRequest용)
     * @param userRequest OAuth2 사용자 요청
     * @return Access Token 또는 null
     */
    private String extractAccessToken(OAuth2UserRequest userRequest) {
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
}
