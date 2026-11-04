package me.bombom.api.v1.image.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageUploadOwnerType {

    MEMBER("member"),
    GUEST("guest"),
    ;

    private final String keyPrefix;
}
