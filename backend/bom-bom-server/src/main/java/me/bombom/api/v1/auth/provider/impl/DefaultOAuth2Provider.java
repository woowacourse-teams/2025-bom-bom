package me.bombom.api.v1.auth.provider.impl;

import me.bombom.api.v1.auth.enums.OAuth2ProviderInfo;
import me.bombom.api.v1.auth.provider.OAuth2Provider;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
public class DefaultOAuth2Provider implements OAuth2Provider {

    @Override
    public OAuth2ProviderInfo getProviderType() {
        return OAuth2ProviderInfo.DEFAULT;
    }


    @Override
    public void revokeToken(Member member) {
        // 알 수 없는 제공자이므로 토큰 철회 불가능
        // 실제로는 이런 경우가 발생하면 안 되지만 안전장치로 구현
    }

    @Override
    public void processWithdrawal(Member member) {
        // 알 수 없는 제공자에 대한 탈퇴 처리
        // 토큰 철회는 불가능하지만 내부 데이터는 소프트 삭제
        revokeToken(member);
    }
}
