package me.bombom.api.v1.subscribe.dto;

public record UnsubscribeResponse(
    String unsubscribeUrl,
    boolean hasUnsubscribeUrl
) {
    public UnsubscribeResponse(String unsubscribeUrl) {
        this(unsubscribeUrl, unsubscribeUrl != null && !unsubscribeUrl.isBlank());
    }
}

