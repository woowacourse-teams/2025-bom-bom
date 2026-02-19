package me.bombom.api.v1.subscribe.dto;

import java.util.List;

public record UnsubscribePatterns(
        String unsubscribe,
        String success,
        String alreadyUnsubscribed,
        String error,
        List<String> adDomains
) {
}
