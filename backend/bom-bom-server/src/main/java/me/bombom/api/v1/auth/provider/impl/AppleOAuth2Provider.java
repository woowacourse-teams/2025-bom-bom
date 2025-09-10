package me.bombom.api.v1.auth.provider.impl;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.client.AppleRevokeClient;
import me.bombom.api.v1.auth.enums.OAuth2ProviderInfo;
import me.bombom.api.v1.auth.provider.OAuth2Provider;
import me.bombom.api.v1.auth.service.AppleTokenService;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppleOAuth2Provider implements OAuth2Provider {

    private final AppleRevokeClient appleRevokeClient;
    private final Supplier<String> appleClientSecretSupplier;
    private final AppleTokenService appleTokenService;

    @Override
    public OAuth2ProviderInfo getProviderType() {
        return OAuth2ProviderInfo.APPLE;
    }

    @Override
    public void revokeToken(Member member) {
        // Apple Refresh Token이 있으면 Apple API로 토큰 철회 시도
        if (member.getAppleRefreshToken() != null && !member.getAppleRefreshToken().isEmpty()) {
            try {
                System.out.println("=== Apple Token 철회 시작 ===");
                System.out.println("memberId: " + member.getId());
                System.out.println("appleRefreshToken: " + member.getAppleRefreshToken());
                
                // AppleTokenService를 통해 토큰 철회
                appleTokenService.revokeToken(member.getAppleRefreshToken());
                
                System.out.println("=== Apple Token 철회 성공 ===");
            } catch (Exception e) {
                System.out.println("Apple Token 철회 실패: " + e.getMessage());
                // Apple API 호출 실패해도 탈퇴는 계속 진행
            }
        } else {
            System.out.println("=== Apple Refresh Token 없음 - 내부 데이터만 삭제 ===");
            System.out.println("memberId: " + member.getId());
            System.out.println("Apple ID에서 앱 제거는 사용자가 직접 처리해야 합니다.");
        }
        // Refresh Token이 없어도 탈퇴는 가능 (내부 데이터만 삭제)
    }

    @Override
    public void processWithdrawal(Member member) {
        revokeToken(member);
    }
}
