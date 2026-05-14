package me.bombom.api.v1.auth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OAuth2Provider {

    GOOGLE("google"),
    APPLE("apple"),
    DEFAULT("default"),
    ;

    private final String value;

    public boolean isEqualProvider(String provider) {
        return this.value.equalsIgnoreCase(provider);
    }
}
