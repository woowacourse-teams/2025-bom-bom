package me.bombom.api.v1.article.event;

import java.time.LocalDateTime;

public record MarkAsReadEvent(

        Long memberId,
        Long articleId,
        LocalDateTime readAt,
        boolean countable
) {

    public static MarkAsReadEvent of(
            Long memberId,
            Long articleId,
            LocalDateTime readAt,
            boolean countable
    ) {
        return new MarkAsReadEvent(memberId, articleId, readAt, countable);
    }
}
