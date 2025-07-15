package me.bombom.api.v1.newsletter.dto;

public record TodayArticleNewsletterResponse(
        String name,
        String imageUrl,
        Long categoryId
) {
}
