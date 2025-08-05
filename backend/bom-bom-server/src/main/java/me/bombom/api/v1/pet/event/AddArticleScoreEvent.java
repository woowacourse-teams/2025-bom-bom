package me.bombom.api.v1.pet.event;

import lombok.Getter;
import me.bombom.api.v1.member.domain.Member;

@Getter
public class AddArticleScoreEvent {

    private final Long memberId;

    public AddArticleScoreEvent(Long memberId) {
        this.memberId = memberId;
    }
}
