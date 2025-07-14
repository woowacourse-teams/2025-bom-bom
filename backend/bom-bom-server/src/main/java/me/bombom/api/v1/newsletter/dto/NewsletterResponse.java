package me.bombom.api.v1.newsletter.dto;

import java.util.List;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record NewsletterResponse(
        Long newsletterId,
        String name,
        String imageUrl,
        String description,
        String mainUrl
) {
    public static List<NewsletterResponse> from(List<Newsletter> newsletters) {
        return newsletters.stream()
                .map(NewsletterResponse::from)
                .toList();
    }

   public static NewsletterResponse from(Newsletter newsLetter) {
       return new NewsletterResponse(newsLetter.getId(), newsLetter.getName(),
               newsLetter.getImageUrl(), newsLetter.getDescription(),
               newsLetter.getMainUrl());
   }
}
