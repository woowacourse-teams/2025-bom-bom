package me.bombom.api.v1.subscribe.event;

public record AutoUnsubscribeCompletedEvent(

        Long subscribeId,
        boolean isSuccess
) {

    public static AutoUnsubscribeCompletedEvent of(Long subscribeId, boolean isSuccess) {
        return new AutoUnsubscribeCompletedEvent(subscribeId, isSuccess);
    }
}
