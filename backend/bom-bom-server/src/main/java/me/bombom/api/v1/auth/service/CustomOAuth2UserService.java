package me.bombom.api.v1.auth.service;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    private final MemberRepository memberRepository;
    private final HttpSession session;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2ProviderInfo oAuth2Provider = OAuth2ProviderInfo.from(provider);
        
        OAuth2User oAuth2User;
        String providerId;
        String profileUrl = "";
        
        if (oAuth2Provider == OAuth2ProviderInfo.APPLE) {
            // Apple의 경우 super.loadUser() 호출하지 않음 (Apple은 user-info 엔드포인트가 없음)
            try {
                // Apple ID Token에서 사용자 정보 추출
                Object idTokenObj = userRequest.getAdditionalParameters().get("id_token");
                if (idTokenObj != null) {
                    // ID Token이 있는 경우 간단한 파싱
                    String idToken = idTokenObj.toString();
                    // 간단한 JWT 디코딩 (서명 검증 없이)
                    String[] parts = idToken.split("\\.");
                    if (parts.length >= 2) {
                        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                        // JSON 파싱 없이 간단하게 sub 추출
                        if (payload.contains("\"sub\"")) {
                            int subStart = payload.indexOf("\"sub\":\"") + 7;
                            int subEnd = payload.indexOf("\"", subStart);
                            providerId = payload.substring(subStart, subEnd);
                        } else {
                            providerId = "apple_" + System.currentTimeMillis();
                        }
                    } else {
                        providerId = "apple_" + System.currentTimeMillis();
                    }
                } else {
                    providerId = "apple_" + System.currentTimeMillis();
                }
                oAuth2User = new CustomOAuth2User(java.util.Map.of("sub", providerId), null);
            } catch (Exception e) {
                providerId = "apple_fallback_" + System.currentTimeMillis();
                oAuth2User = new CustomOAuth2User(java.util.Map.of("sub", providerId), null);
            }
        } else {
            // Google 등 다른 provider는 기존 방식 사용
            oAuth2User = super.loadUser(userRequest);
            providerId = oAuth2User.getAttribute(oAuth2Provider.getIdKey());
            profileUrl = oAuth2User.getAttribute(oAuth2Provider.getProfileImageKey());
        }

        Optional<Member> member = memberRepository.findByProviderAndProviderIdIncludeDeleted(provider, providerId);

        if (member.isPresent() && member.get().isWithdrawnMember()) {
            throw new UnauthorizedException(ErrorDetail.WITHDRAWN_MEMBER);
        }

        if (member.isEmpty()) {
            // Apple 로그인인 경우에만 Refresh Token 추출
            String refreshToken = (oAuth2Provider == OAuth2ProviderInfo.APPLE) 
                ? extractRefreshToken(userRequest) : null;
            System.out.println("=== 신규 회원 처리 ===");
            System.out.println("provider: " + provider);
            System.out.println("providerId: " + providerId);
            System.out.println("refreshToken: " + (refreshToken != null ? "있음" : "없음"));
            
            PendingOAuth2Member pendingMember = PendingOAuth2Member.builder()
                    .provider(provider)
                    .providerId(providerId)
                    .profileUrl(profileUrl)
                    .appleRefreshToken(refreshToken)
                    .build();
            session.setAttribute("pendingMember", pendingMember);
        } else {
            // 기존 회원의 경우 Apple Refresh Token 업데이트
            updateRefreshTokenIfNeeded(member.get(), userRequest);
        }

        return new CustomOAuth2User(oAuth2User.getAttributes(), member.orElse(null));
    }

    private String extractRefreshToken(OAuth2UserRequest userRequest) {
        try {
            Object refreshTokenObj = userRequest.getAdditionalParameters().get(REFRESH_TOKEN_KEY);
            System.out.println("=== Apple Refresh Token 추출 ===");
            System.out.println("additionalParameters keys: " + userRequest.getAdditionalParameters().keySet());
            System.out.println("refreshToken 존재: " + (refreshTokenObj != null));
            if (refreshTokenObj != null) {
                System.out.println("refreshToken length: " + refreshTokenObj.toString().length());
            }
            return refreshTokenObj != null ? refreshTokenObj.toString() : null;
        } catch (Exception e) {
            System.out.println("Refresh Token 추출 실패: " + e.getMessage());
            return null;
        }
    }

    private void updateRefreshTokenIfNeeded(Member member, OAuth2UserRequest userRequest) {
        try {
            String refreshToken = extractRefreshToken(userRequest);
            System.out.println("=== 기존 회원 Refresh Token 업데이트 ===");
            System.out.println("memberId: " + member.getId());
            System.out.println("provider: " + member.getProvider());
            System.out.println("새로운 refreshToken: " + (refreshToken != null ? "있음" : "없음"));
            System.out.println("기존 appleRefreshToken: " + (member.getAppleRefreshToken() != null ? "있음" : "없음"));
            
            if (refreshToken != null && member.getProvider().equals(OAuth2ProviderInfo.APPLE.getProvider())) {
                if (!refreshToken.equals(member.getAppleRefreshToken())) {
                    System.out.println("Apple Refresh Token 업데이트 실행");
                    member.updateAppleRefreshToken(refreshToken);
                    memberRepository.save(member);
                    System.out.println("Apple Refresh Token 업데이트 완료");
                } else {
                    System.out.println("Apple Refresh Token 동일 - 업데이트 불필요");
                }
            } else {
                System.out.println("Refresh Token 업데이트 조건 불만족");
            }
        } catch (Exception e) {
            System.out.println("Refresh Token 업데이트 실패: " + e.getMessage());
        }
    }
}
