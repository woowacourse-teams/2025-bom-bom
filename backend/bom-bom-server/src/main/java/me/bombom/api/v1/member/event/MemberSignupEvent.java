package me.bombom.api.v1.member.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MemberSignupEvent extends ApplicationEvent {

    private final Long memberId;

    public MemberSignupEvent(Object source, Long memberId) {
        super(source);
        this.memberId = memberId;
    }
}

