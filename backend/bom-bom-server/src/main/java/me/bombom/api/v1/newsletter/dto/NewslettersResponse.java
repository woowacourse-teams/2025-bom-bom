package me.bombom.api.v1.newsletter.dto;

import java.util.List;

public record NewslettersResponse(

        List<CategoryResponse> categories,
        List<NewsletterResponse> newsletters
) {

    public static NewslettersResponse of(List<CategoryResponse> categories, List<NewsletterResponse> newsletters) {
        return new NewslettersResponse(categories, newsletters);
    }
}
