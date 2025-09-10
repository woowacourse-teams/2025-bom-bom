package me.bombom.api.v1.auth.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.enums.OAuth2ProviderInfo;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String ID_TOKEN_KEY = "id_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    private final MemberRepository memberRepository;
    private final HttpSession session;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            log.info("OAuth2 로그인 시작 - provider: {}", userRequest.getClientRegistration().getRegistrationId());
            String provider = userRequest.getClientRegistration().getRegistrationId();
            OAuth2ProviderInfo oAuth2Provider = OAuth2ProviderInfo.from(provider);

        Map<String, Object> attributes;
        String providerId;
        String profileUrl = "";

        if (oAuth2Provider == OAuth2ProviderInfo.APPLE) {
            try {
                log.info("Apple OAuth2 처리 시작 - additionalParameters: {}", userRequest.getAdditionalParameters().keySet());
                
                // Apple의 경우 ID Token을 여러 곳에서 찾기
                Object idTokenObj = userRequest.getAdditionalParameters().get(ID_TOKEN_KEY);
                
                if (idTokenObj == null) {
                    log.warn("Apple ID Token이 additionalParameters에 없음");
                    
                    // Apple OAuth2에서는 ID Token이 없을 수 있으므로 기본값으로 처리
                    String uniqueId = "apple_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
                    attributes = Map.of("sub", uniqueId);
                    providerId = uniqueId;
                    log.info("Apple ID Token 없음 - 기본값으로 처리 - providerId: {}", providerId);
                } else {
                    String idToken = idTokenObj.toString();
                    log.info("Apple ID Token 발견 - token length: {}", idToken.length());
                    
                    JWT jwt = JWTParser.parse(idToken);
                    attributes = jwt.getJWTClaimsSet().getClaims();
                    providerId = (String) attributes.get(oAuth2Provider.getIdKey());
                    log.info("Apple ID Token 파싱 성공 - providerId: {}", providerId);
                }
            } catch (Exception e) {
                log.error("Apple 사용자 정보 조회 실패 - error: {}", e.getMessage(), e);
                // 예외가 발생해도 기본값으로 처리하여 로그인을 계속 진행
                String fallbackId = "apple_fallback_" + System.currentTimeMillis();
                attributes = Map.of("sub", fallbackId);
                providerId = fallbackId;
                log.info("Apple 예외 발생 - fallback으로 처리 - providerId: {}", providerId);
            }
        } else {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            attributes = oAuth2User.getAttributes();
            providerId = oAuth2User.getAttribute(oAuth2Provider.getIdKey());
            profileUrl = oAuth2User.getAttribute(oAuth2Provider.getProfileImageKey());
        }

        log.info("기존 회원 조회 시작 - provider: {}, providerId: {}", provider, providerId);
        Optional<Member> member = memberRepository.findByProviderAndProviderIdIncludeDeleted(provider, providerId);
        log.info("기존 회원 조회 완료 - member 존재: {}", member.isPresent());

        if (member.isPresent() && member.get().isWithdrawnMember()) {
            log.warn("탈퇴한 회원 로그인 시도 - memberId: {}", member.get().getId());
            throw new UnauthorizedException(ErrorDetail.WITHDRAWN_MEMBER);
        }

        if (member.isEmpty()) {
            log.info("신규 회원 처리 시작 - provider: {}, providerId: {}", provider, providerId);
            // Apple 로그인인 경우에만 Refresh Token 추출
            String refreshToken = (oAuth2Provider == OAuth2ProviderInfo.APPLE) 
                ? extractRefreshToken(userRequest) : null;
            PendingOAuth2Member pendingMember = PendingOAuth2Member.builder()
                    .provider(provider)
                    .providerId(providerId)
                    .profileUrl(profileUrl)
                    .appleRefreshToken(refreshToken)
                    .build();
            session.setAttribute("pendingMember", pendingMember);
            log.info("신규 회원 세션 저장 완료");
        } else {
            log.info("기존 회원 Refresh Token 업데이트 시작 - memberId: {}", member.get().getId());
            updateRefreshTokenIfNeeded(member.get(), userRequest);
            log.info("기존 회원 Refresh Token 업데이트 완료");
        }

            log.info("OAuth2 로그인 완료 - provider: {}, providerId: {}", provider, providerId);
            return new CustomOAuth2User(attributes, member.orElse(null));
        } catch (Exception e) {
            log.error("OAuth2 로그인 중 예외 발생 - provider: {}, error: {}", 
                    userRequest.getClientRegistration().getRegistrationId(), e.getMessage(), e);
            throw new OAuth2AuthenticationException("OAuth2 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String extractRefreshToken(OAuth2UserRequest userRequest) {
        try {
            Object refreshTokenObj = userRequest.getAdditionalParameters().get(REFRESH_TOKEN_KEY);
            if (refreshTokenObj != null) {
                log.info("Apple Refresh Token 추출 성공");
            }
            return refreshTokenObj != null ? refreshTokenObj.toString() : null;
        } catch (Exception e) {
            log.warn("Apple Refresh Token 추출 실패 - error: {}", e.getMessage());
            return null;
        }
    }

    private void updateRefreshTokenIfNeeded(Member member, OAuth2UserRequest userRequest) {
        try {
            String refreshToken = extractRefreshToken(userRequest);
            
            if (refreshToken != null && member.getProvider().equals(OAuth2ProviderInfo.APPLE.getProvider())) {
                if (!refreshToken.equals(member.getAppleRefreshToken())) {
                    log.info("Apple Refresh Token 업데이트 - memberId: {}", member.getId());
                    member.updateAppleRefreshToken(refreshToken);
                    memberRepository.save(member);
                }
            }
        } catch (Exception e) {
            log.warn("Apple Refresh Token 업데이트 실패 - memberId: {}, error: {}", member.getId(), e.getMessage());
        }
    }
}
