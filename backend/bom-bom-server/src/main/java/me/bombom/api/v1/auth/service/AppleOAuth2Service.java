package me.bombom.api.v1.auth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.client.AppleAuthClient;
import me.bombom.api.v1.auth.client.dto.AppleNativeTokenResponse;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.dto.request.NativeLoginRequest;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Apple OAuth2 통합 서비스
 * Apple 로그인, Token Revoke, 탈퇴 처리를 모두 담당합니다
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppleOAuth2Service extends OidcUserService {

    private static final String ACCESS_TOKEN_KEY = "access_token";
    
    private final MemberRepository memberRepository;
    private final HttpSession session;
    private final IdTokenValidator idTokenValidator;
    private final ObjectMapper objectMapper;
    private final AppleAuthClient appleAuthClient;

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
            
            // Apple user 정보 파싱 및 attributes 병합
            Map<String, Object> appleUserInfo = parseAppleUserInfo(oidcUser.getAttributes());

            // 기존 회원 확인
            Optional<Member> member = findMemberAndSetPendingIfNew(providerId);
            return new CustomOAuth2User(appleUserInfo, member.orElse(null), oidcUser.getIdToken(), oidcUser.getUserInfo());
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
     */
    public void revokeToken(String accessToken) {
        String clientId = (String) session.getAttribute("appleClientId");
        if (clientId == null || clientId.isBlank()) {
            clientId = this.clientId; // 웹 기본 clientId
        }
        try {
            log.info("Apple Token Revoke 시작 - clientId: {}", clientId);
            appleAuthClient.revokeToken(accessToken, clientId);
            log.info("Apple Token Revoke 성공");
        } catch (Exception e) {
            log.warn("Apple Token Revoke 실패 - error: {}", e.getMessage());
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
            AppleNativeTokenResponse tokenResponse = appleAuthClient.getTokenResponse(request.authorizationCode(), bundleId);
            session.setAttribute("appleAccessToken", tokenResponse.accessToken());
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
     * 세션에서 Apple user 정보를 파싱하여 attributes에 병합
     */
    private Map<String, Object> parseAppleUserInfo(Map<String, Object> baseAttributes) {
        Map<String, Object> mergedAttributes = new HashMap<>(baseAttributes);
        
        try {
            Object userParam = session.getAttribute("appleUserParam");
            if (!(userParam instanceof String userJson) || userJson.isBlank()) {
                log.info("Apple user 파라미터가 없습니다(최초 동의가 아니거나 Apple 미제공 케이스)");
                return mergedAttributes;
            }
            
            log.info("Apple user 원문(JSON) 수신: {}", userJson);
            //objectMapper가 Map<String, Object>로 만들어주기 위해서는 TypeReference를 사용해야 함. 없어도 Map으로 만들 수는 있음
            Map<String, Object> userMap = objectMapper.readValue(userJson, new TypeReference<>() {});
            parseEmail(userMap, mergedAttributes);
            parseName(userMap, mergedAttributes);

            // 사용 후 세션에서 제거
            session.removeAttribute("appleUserParam");
        } catch (Exception e) {
            log.warn("Apple user 파라미터 파싱 실패 - error: {}", e.getMessage(), e);
        }
        return mergedAttributes;
    }

    private void parseEmail(Map<String, Object> userMap, Map<String, Object> attributes) {
        Object emailFromUser = userMap.get("email");
        if (emailFromUser instanceof String emailStr && !emailStr.isBlank()) {
            attributes.put("email", emailStr);
            log.info("Apple user 파싱 - email: {}", emailStr);
        }
    }

    private void parseName(Map<String, Object> userMap, Map<String, Object> attributes) {
        Object nameObj = userMap.get("name");
        //name은 firstName과 lastName으로 Map으로 옴
        if (!(nameObj instanceof Map)) return;

        //nameObj는 Object 타입인데 Map으로 바꾸려하면 unchecked 예외를 발생시킴. 그걸 무시함
        @SuppressWarnings("unchecked")
        Map<String, Object> nameMap = (Map<String, Object>) nameObj;

        String fullName = getFullName(nameMap);

        if (!fullName.isEmpty()) {
            attributes.put("name", fullName);
            log.info("Apple user 파싱 - fullName: {}", fullName);
        }
    }

    private String getFullName(Map<String, Object> nameMap) {
        String firstName = Optional.ofNullable(nameMap.get("firstName"))
                .map(Object::toString)
                .orElse("");
        String lastName = Optional.ofNullable(nameMap.get("lastName"))
                .map(Object::toString)
                .orElse("");

        // lastName + firstName 순서로 합치고 공백 제거
        return (lastName + firstName).strip();
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
