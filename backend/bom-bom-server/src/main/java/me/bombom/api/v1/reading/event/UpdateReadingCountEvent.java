package me.bombom.api.v1.reading.event;

import lombok.Getter;

@Getter
public class UpdateReadingCountEvent {

    private final Long articleId;

    public UpdateReadingCountEvent(Long articleId) {
        this.articleId = articleId;
    }
}
