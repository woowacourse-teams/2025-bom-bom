package me.bombom.api.v1.subscribe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.util.StringUtils;

public record UnsubscribeResponse(

    String unsubscribeUrl,

    @Schema(required = true)
    boolean hasUnsubscribeUrl
) {

    public static UnsubscribeResponse of(String unsubscribeUrl) {
        return new UnsubscribeResponse(unsubscribeUrl, StringUtils.hasText(unsubscribeUrl));
    }
}

