package me.bombom.api.v1.article.event;

import java.time.LocalDateTime;

public record MarkAsReadEvent(

        Long memberId,
        Long articleId,
        LocalDateTime readAt
) {
}
