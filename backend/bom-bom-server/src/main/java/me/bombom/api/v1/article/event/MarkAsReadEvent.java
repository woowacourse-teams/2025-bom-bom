package me.bombom.api.v1.article.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MarkAsReadEvent {
    private final Long memberId;
    private final Long articleId;
}
