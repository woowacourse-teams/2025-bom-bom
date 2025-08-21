package me.bombom.api.v1.article.event;

public record MarkAsReadEvent(
        Long memberId,
        Long articleId
) {
}
