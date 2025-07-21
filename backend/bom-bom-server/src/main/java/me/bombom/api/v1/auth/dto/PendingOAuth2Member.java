package me.bombom.api.v1.auth.dto;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PendingOAuth2Member implements Serializable {

    private final String provider;
    private final String providerId;
    private final String profileUrl;
}
