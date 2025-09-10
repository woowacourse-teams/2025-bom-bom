package me.bombom.api.v1.auth.provider;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import me.bombom.api.v1.auth.enums.OAuth2ProviderInfo;
import org.springframework.stereotype.Component;

@Component
public class OAuth2ProviderFactory {

    private final Map<String, OAuth2Provider> providers;

    public OAuth2ProviderFactory(List<OAuth2Provider> providerList) {
        this.providers = providerList.stream()
            .collect(Collectors.toMap(
                provider -> provider.getProviderType().getProvider(),
                Function.identity()
            ));
    }

    public OAuth2Provider getProvider(OAuth2ProviderInfo providerType) {
        if (providerType == null) {
            return providers.get(OAuth2ProviderInfo.DEFAULT.getProvider());
        }
        
        OAuth2Provider provider = providers.get(providerType.getProvider());
        if (provider == null) {
            return providers.get(OAuth2ProviderInfo.DEFAULT.getProvider());
        }
        
        return provider;
    }
}
