package me.bombom.api.v1.subscribe.event;

public record UnsubscribeRequestedEvent(

        Long subscribeId,
        String unsubscribeUrl,
        Long newsletterId
) {

    public static UnsubscribeRequestedEvent of (
            Long subscribeId,
            String unsubscribeUrl,
            Long newsletterId
    ) {
        return new UnsubscribeRequestedEvent(subscribeId, unsubscribeUrl, newsletterId);
    }
}
