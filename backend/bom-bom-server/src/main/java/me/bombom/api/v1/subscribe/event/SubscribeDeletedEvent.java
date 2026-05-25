package me.bombom.api.v1.subscribe.event;

import java.time.LocalDate;

public record SubscribeDeletedEvent(

        Long newsletterId,
        LocalDate birthDate
) {

    public static SubscribeDeletedEvent of(Long newsletterId, LocalDate birthDate) {
        return new SubscribeDeletedEvent(newsletterId, birthDate);
    }
}
