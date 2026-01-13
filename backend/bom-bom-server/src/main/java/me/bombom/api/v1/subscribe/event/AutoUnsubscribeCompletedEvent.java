package me.bombom.api.v1.subscribe.event;

public record AutoUnsubscribeCompletedEvent(
        Long subscribeId,
        boolean success,
        String errorMessage
) {

    public static AutoUnsubscribeCompletedEvent success(Long subscribeId) {
        return new AutoUnsubscribeCompletedEvent(subscribeId, true, null);
    }

    public static AutoUnsubscribeCompletedEvent failure(Long subscribeId, String errorMessage) {
        return new AutoUnsubscribeCompletedEvent(subscribeId, false, errorMessage);
    }
}
