package me.bombom.api.v1.newsletter.dto;

import java.util.List;

public record NewsletterListResponse(

        List<CategoryResponse> categories,
        List<NewsletterResponse> newsletters
) {

    public static NewsletterListResponse of(List<CategoryResponse> categories, List<NewsletterResponse> newsletters) {
        return new NewsletterListResponse(categories, newsletters);
    }
}
