package me.bombom.api.v1.auth.provider;

import me.bombom.api.v1.auth.enums.OAuth2ProviderInfo;

public interface OAuth2Provider {

    OAuth2ProviderInfo getProviderType();
}
