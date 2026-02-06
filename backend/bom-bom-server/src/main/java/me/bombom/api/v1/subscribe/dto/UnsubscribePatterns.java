package me.bombom.api.v1.subscribe.dto;

public record UnsubscribePatterns(
        String unsubscribe,
        String success,
        String alreadyUnsubscribed,
        String error
) {
}
