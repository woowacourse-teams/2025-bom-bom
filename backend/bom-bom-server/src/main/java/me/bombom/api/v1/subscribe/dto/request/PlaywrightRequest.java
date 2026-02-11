package me.bombom.api.v1.subscribe.dto.request;

import me.bombom.api.v1.subscribe.dto.UnsubscribePatterns;

public record PlaywrightRequest(

        String url,
        UnsubscribePatterns patterns
) {

    public static PlaywrightRequest of(String url, UnsubscribePatterns patterns) {
        return new PlaywrightRequest(url, patterns);
    }
}
