package me.bombom.api.v1.auth.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
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
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class AppleOAuth2Service implements OAuth2LoginService {
    
    private static final String ID_TOKEN_KEY = "id_token";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke";
    
    private final MemberRepository memberRepository;
    private final HttpSession session;
    private final RestClient restClient;
    private final Supplier<String> appleClientSecretSupplier;
    
    @Value("${oauth2.apple.client-id}")
    private String clientId;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("Apple OIDC 로그인 처리 시작");
        
        try {
            Object idTokenObj = userRequest.getAdditionalParameters().get(ID_TOKEN_KEY);
            if (idTokenObj == null) {
                log.error("Apple ID Token이 additionalParameters에 없음 - available keys: {}", userRequest.getAdditionalParameters().keySet());
                throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                    .addContext("provider", "apple")
                    .addContext("reason", "apple_id_token_not_found_in_additional_parameters");
            }
            
            String idToken = idTokenObj.toString();
            JWT jwt = JWTParser.parse(idToken);
            Map<String, Object> attributes = jwt.getJWTClaimsSet().getClaims();
            String providerId = (String) attributes.get("sub");
            String email = (String) attributes.get("email");
            log.info("Apple ID Token 파싱 성공 - providerId: {}, email: {}", providerId, email);
            
            // Apple Access Token 세션에 저장
            String accessToken = extractAccessToken(userRequest);
            if (accessToken != null) {
                session.setAttribute("appleAccessToken", accessToken);
                log.info("Apple Access Token 세션에 저장 완료");
            } else {
                log.warn("Apple Access Token 추출 실패");
            }
            
            // 기존 회원 확인
            Optional<Member> member = memberRepository.findByProviderAndProviderId("apple", providerId);
            
            if (member.isEmpty()) {
                PendingOAuth2Member pendingMember = PendingOAuth2Member.builder()
                        .provider("apple")
                        .providerId(providerId)
                        .profileUrl("")
                        .build();
                session.setAttribute("pendingMember", pendingMember);
                log.info("Apple 신규 회원 - 회원가입 대기 상태로 설정");
            } else {
                log.info("Apple 기존 회원 - 로그인 성공, memberId: {}", member.get().getId());
            }
            
            return new CustomOAuth2User(attributes, member.orElse(null));
        } catch (Exception e) {
            log.error("Apple OIDC 로그인 처리 실패 - error: {}", e.getMessage(), e);
            throw new UnauthorizedException(ErrorDetail.INVALID_TOKEN)
                .addContext("provider", "apple")
                .addContext("reason", "apple_oidc_processing_failed");
        }
    }
    
    @Override
    public String getProviderType() {
        return "apple";
    }
    
    @Override
    public boolean supports(String provider) {
        return "apple".equals(provider);
    }

    /**
     * Apple Access Token을 철회합니다
     * @param accessToken 철회할 Access Token
     * @return 철회 성공 여부
     */
    public boolean revokeToken(String accessToken) {
        try {
            log.info("Apple Token Revoke 시작 - clientId: {}", clientId);

            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("token", accessToken);
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", appleClientSecretSupplier.get());

            restClient.post()
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
     * Apple Access Token을 추출합니다
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
