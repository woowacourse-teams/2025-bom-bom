package me.bombom.api.v1.auth.provider.impl;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.client.AppleRevokeClient;
import me.bombom.api.v1.auth.enums.OAuth2ProviderInfo;
import me.bombom.api.v1.auth.provider.OAuth2Provider;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppleOAuth2Provider implements OAuth2Provider {

    private final AppleRevokeClient appleRevokeClient;
    private final Supplier<String> appleClientSecretSupplier;

    @Override
    public OAuth2ProviderInfo getProviderType() {
        return OAuth2ProviderInfo.APPLE;
    }

    @Override
    public void revokeToken(Member member) {
        // Apple Refresh Token이 있으면 Apple API로 토큰 철회 시도
        if (member.getAppleRefreshToken() != null && !member.getAppleRefreshToken().isEmpty()) {
            try {
                appleRevokeClient.revoke(member.getAppleRefreshToken(), appleClientSecretSupplier.get());
            } catch (Exception e) {
                // Apple API 호출 실패해도 탈퇴는 계속 진행
                // 로그만 남기고 예외를 던지지 않음
            }
        }
        // Refresh Token이 없어도 탈퇴는 가능 (내부 데이터만 삭제)
    }

    @Override
    public void processWithdrawal(Member member) {
        revokeToken(member);
    }
}
