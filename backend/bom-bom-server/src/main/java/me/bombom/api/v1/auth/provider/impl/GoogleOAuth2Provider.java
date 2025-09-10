package me.bombom.api.v1.auth.provider.impl;

import me.bombom.api.v1.auth.enums.OAuth2ProviderInfo;
import me.bombom.api.v1.auth.provider.OAuth2Provider;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2Provider implements OAuth2Provider {

    @Override
    public OAuth2ProviderInfo getProviderType() {
        return OAuth2ProviderInfo.GOOGLE;
    }


    @Override
    public void revokeToken(Member member) {
        // Google은 토큰 철회 API가 없으므로 아무것도 하지 않음
        // Google 계정 삭제는 사용자가 직접 Google 계정 설정에서 해야 함
    }

    @Override
    public void processWithdrawal(Member member) {
        // Google 회원 탈퇴 시에는 토큰 철회가 불필요
        // 단순히 내부 데이터만 소프트 삭제하면 됨
    }
}
