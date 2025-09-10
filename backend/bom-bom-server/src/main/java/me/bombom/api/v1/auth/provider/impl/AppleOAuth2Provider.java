package me.bombom.api.v1.auth.provider.impl;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.auth.client.AppleRevokeClient;
import me.bombom.api.v1.auth.enums.OAuth2ProviderInfo;
import me.bombom.api.v1.auth.provider.OAuth2Provider;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
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
        if (member.getAppleRefreshToken() == null || member.getAppleRefreshToken().isEmpty()) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_TOKEN);
        }
        appleRevokeClient.revoke(member.getAppleRefreshToken(), appleClientSecretSupplier.get());
    }

    @Override
    public void processWithdrawal(Member member) {
        revokeToken(member);
    }
}
