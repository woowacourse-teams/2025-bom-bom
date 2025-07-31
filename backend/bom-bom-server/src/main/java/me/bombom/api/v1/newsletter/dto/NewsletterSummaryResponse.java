package me.bombom.api.v1.newsletter.dto;

import com.querydsl.core.annotations.QueryProjection;

public record NewsletterSummaryResponse(
        String name,
        String imageUrl,
        String category
) {

    @QueryProjection
    public NewsletterSummaryResponse {}
}
