package me.bombom.api.v1.newsletterrequest.dto.response;

import java.util.List;

public record NewsletterRequestSuggestionResponse(
        List<NewsletterRequestResponse> requests,
        List<NewsletterSuggestionResponse> newsletters
) {

    public static NewsletterRequestSuggestionResponse empty() {
        return new NewsletterRequestSuggestionResponse(List.of(), List.of());
    }
}
