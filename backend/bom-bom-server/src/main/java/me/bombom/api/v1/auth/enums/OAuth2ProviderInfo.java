package me.bombom.api.v1.auth.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;

@Getter
@AllArgsConstructor
public enum OAuth2ProviderInfo {

    GOOGLE("google", "Google", "sub", "picture"),
    APPLE("apple", "Apple", "sub", null),
    DEFAULT("default", "Default", null, null),
    ;

    private final String provider;
    private final String displayName;
    private final String idKey;
    private final String profileImageKey;

    public static OAuth2ProviderInfo from(String provider) {
        return Arrays.stream(OAuth2ProviderInfo.values())
                .filter(oAuth2Provider -> oAuth2Provider.getProvider().equals(provider))
                .findFirst()
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.UNSUPPORTED_OAUTH2_PROVIDER)
                    .addContext("requestedProvider", provider)
                    .addContext("supportedProviders", Arrays.toString(OAuth2ProviderInfo.values())));
    }

    public static OAuth2ProviderInfo fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return DEFAULT;
        }
        for (OAuth2ProviderInfo type : values()) {
            if (type.provider.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return DEFAULT;
    }
}