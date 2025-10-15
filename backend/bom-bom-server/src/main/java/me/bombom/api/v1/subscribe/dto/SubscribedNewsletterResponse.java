package me.bombom.api.v1.subscribe.dto;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotNull;

public record SubscribedNewsletterResponse(

        @NotNull
        String name,

        String imageUrl,

        @NotNull
        String category
) {
        @QueryProjection
        public SubscribedNewsletterResponse {}
}
