package me.bombom.api.v1.subscribe.dto.response;

public record UnsubscribeResponse(

    String unsubscribeUrl
) {

    public static UnsubscribeResponse of(String unsubscribeUrl) {
        return new UnsubscribeResponse(unsubscribeUrl);
    }
}

