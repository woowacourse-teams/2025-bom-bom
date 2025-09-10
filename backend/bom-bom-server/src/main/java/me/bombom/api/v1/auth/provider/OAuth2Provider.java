package me.bombom.api.v1.auth.provider;

import me.bombom.api.v1.auth.enums.OAuth2ProviderInfo;
import me.bombom.api.v1.member.domain.Member;

public interface OAuth2Provider {

    OAuth2ProviderInfo getProviderType();

    void revokeToken(Member member);

    void processWithdrawal(Member member);
}
