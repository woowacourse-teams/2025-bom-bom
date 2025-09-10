package me.bombom.api.v1.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.service.MemberService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final MemberService memberService;
    private final AppleTokenService appleTokenService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("=== CustomOidcUserService.loadUser 호출됨 ===");
        System.out.println("=== Apple OIDC 로그인 처리 시작 ===");
        
        // 기본 OidcUser 로드
        OidcUser oidcUser = super.loadUser(userRequest);
        
        System.out.println("OidcUser - sub: " + oidcUser.getSubject());
        System.out.println("OidcUser - email: " + oidcUser.getEmail());
        System.out.println("OidcUser - name: " + oidcUser.getFullName());
        System.out.println("OidcUser - attributes: " + oidcUser.getAttributes());
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        System.out.println("registrationId: " + registrationId);
        
        try {
            // Apple OIDC의 경우 사용자 정보 처리
            if ("apple".equals(registrationId)) {
                System.out.println("=== Apple OIDC 사용자 정보 처리 ===");
                
                // Apple ID Token에서 사용자 정보 추출
                String sub = oidcUser.getSubject();
                String email = oidcUser.getEmail();
                String name = oidcUser.getFullName();
                
                System.out.println("Apple 사용자 정보 - sub: " + sub + ", email: " + email + ", name: " + name);
                
                // Apple Refresh Token 가져오기 시도
                String refreshToken = null;
                try {
                    System.out.println("=== Apple Refresh Token 가져오기 시도 ===");
                    // Authorization Code가 필요하지만, OIDC에서는 Access Token만 있음
                    // Apple의 경우 Refresh Token은 첫 로그인 시에만 제공되므로 여기서는 null로 처리
                    System.out.println("Apple OIDC에서는 Refresh Token을 별도로 가져올 수 없음");
                    System.out.println("Apple Refresh Token: 없음 (OIDC 특성상 첫 로그인 시에만 제공)");
                } catch (Exception e) {
                    System.out.println("Apple Refresh Token 가져오기 실패: " + e.getMessage());
                    log.warn("Apple Refresh Token 가져오기 실패", e);
                }
                
                // 기존 회원 조회 또는 새 회원 생성 (Refresh Token 포함)
                Member member = memberService.findOrCreateMemberByAppleId(sub, email, name, refreshToken);
                
                System.out.println("member: " + (member != null ? "있음 (ID: " + member.getId() + ")" : "없음"));
                System.out.println("member appleRefreshToken: " + (member != null && member.getAppleRefreshToken() != null ? "저장됨" : "없음"));
                
                // CustomOAuth2User로 래핑하여 반환
                return new CustomOAuth2User(oidcUser, member);
            }
            
            // 다른 OIDC 제공자의 경우 기본 처리
            return oidcUser;
            
        } catch (Exception e) {
            log.error("Apple OIDC 사용자 정보 처리 중 오류 발생", e);
            System.out.println("Apple OIDC 사용자 정보 처리 중 오류 발생: " + e.getMessage());
            throw new OAuth2AuthenticationException(new OAuth2Error("apple_oidc_error", "Apple OIDC 사용자 정보 처리 실패", null), e);
        }
    }
}
