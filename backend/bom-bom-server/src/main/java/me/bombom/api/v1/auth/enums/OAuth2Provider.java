package me.bombom.api.v1.auth.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;

@Getter
@AllArgsConstructor
public enum OAuth2Provider {

    GOOGLE("google","sub", "picture"),
    APPLE("apple", "sub", null),
    ;

    private final String provider;
    private final String idKey;
    private final String profileImageKey;

    public static OAuth2Provider from(String provider) {
        return Arrays.stream(OAuth2Provider.values())
                .filter(oAuth2Provider -> oAuth2Provider.getProvider().equals(provider))
                .findFirst()
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.UNSUPPORTED_OAUTH2_PROVIDER)
                    .addContext("requestedProvider", provider)
                    .addContext("supportedProviders", Arrays.toString(OAuth2Provider.values())));
    }
}
