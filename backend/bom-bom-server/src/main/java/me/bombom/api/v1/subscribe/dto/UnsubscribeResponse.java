package me.bombom.api.v1.subscribe.dto;

import lombok.Getter;

@Getter
public class UnsubscribeResponse {

    private final String unsubscribeUrl;
    private final boolean hasUnsubscribeUrl;

    public UnsubscribeResponse(String unsubscribeUrl) {
        this.unsubscribeUrl = unsubscribeUrl;
        this.hasUnsubscribeUrl = (unsubscribeUrl != null && !unsubscribeUrl.isBlank());
    }

    public boolean hasUnsubscribeUrl() {
        return hasUnsubscribeUrl;
    }
}

