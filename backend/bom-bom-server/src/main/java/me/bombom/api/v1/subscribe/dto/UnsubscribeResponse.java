package me.bombom.api.v1.subscribe.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UnsubscribeResponse(

    String unsubscribeUrl,

    @Schema(required = true)
    boolean hasUnsubscribeUrl
) {

    public UnsubscribeResponse(String unsubscribeUrl) {
        this(unsubscribeUrl, unsubscribeUrl != null && !unsubscribeUrl.isBlank());
    }
}

