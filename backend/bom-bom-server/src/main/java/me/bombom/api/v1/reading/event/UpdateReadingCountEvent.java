package me.bombom.api.v1.reading.event;

import lombok.Getter;

@Getter
public class UpdateReadingCountEvent {

    private final Long memberId;
    private final Long articleId;

    public UpdateReadingCountEvent(Long memberId, Long articleId) {
        this.memberId = memberId;
        this.articleId = articleId;
    }
}
