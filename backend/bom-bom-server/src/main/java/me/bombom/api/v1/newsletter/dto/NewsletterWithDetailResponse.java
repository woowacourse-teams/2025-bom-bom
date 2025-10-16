package me.bombom.api.v1.newsletter.dto;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;

public record NewsletterWithDetailResponse(

        @NotNull
        String name,

        @NotNull
        String description,

        @NotNull
        String imageUrl,

        @NotNull
        Long categoryId,

        @NotNull
        String mainPageUrl,

        @NotNull
        String subscribeUrl,

        @NotNull
        String issueCycle,

        String previousNewsletterUrl,

        String subscribeMethod
) {
    public static NewsletterWithDetailResponse of(Newsletter newsletter, NewsletterDetail newsletterDetail) {
        return new NewsletterWithDetailResponse(
                newsletter.getName(),
                newsletter.getDescription(),
                newsletter.getImageUrl(),
                newsletter.getCategoryId(),
                newsletterDetail.getMainPageUrl(),
                newsletterDetail.getSubscribeUrl(),
                newsletterDetail.getIssueCycle(),
                newsletterDetail.getPreviousNewsletterUrl(),
                newsletterDetail.getSubscribeMethod()
        );
    }
}
