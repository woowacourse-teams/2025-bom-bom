package me.bombom.api.v1.subscribe.event;

import java.time.LocalDate;

public record SubscribeCreatedEvent(

        Long newsletterId,
        LocalDate birthDate
) {

    public static SubscribeCreatedEvent of(Long newsletterId, LocalDate birthDate) {
        return new SubscribeCreatedEvent(newsletterId, birthDate);
    }
}
