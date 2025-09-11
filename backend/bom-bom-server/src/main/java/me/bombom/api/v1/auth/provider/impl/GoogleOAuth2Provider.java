package me.bombom.api.v1.auth.provider.impl;

import me.bombom.api.v1.auth.enums.OAuth2ProviderInfo;
import me.bombom.api.v1.auth.provider.OAuth2Provider;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2Provider implements OAuth2Provider {

    @Override
    public OAuth2ProviderInfo getProviderType() {
        return OAuth2ProviderInfo.GOOGLE;
    }
}
