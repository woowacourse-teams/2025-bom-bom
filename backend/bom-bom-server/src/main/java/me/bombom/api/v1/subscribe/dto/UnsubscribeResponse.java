package me.bombom.api.v1.subscribe.dto;

public record UnsubscribeResponse(

    String unsubscribeUrl
) {

    public static UnsubscribeResponse of(String unsubscribeUrl) {
        return new UnsubscribeResponse(unsubscribeUrl);
    }
}

