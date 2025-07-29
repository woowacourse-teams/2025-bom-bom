package me.bombom.api.v1.member.event;

import lombok.Getter;

@Getter
public class MemberSignupEvent {

    private final Long memberId;

    public MemberSignupEvent(Long memberId) {
        this.memberId = memberId;
    }
}

