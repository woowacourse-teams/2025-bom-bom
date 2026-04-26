package me.bombom.api.v1.nativenewsletter.maeilmail.event;

import java.time.LocalDate;

public record MaeilMailSubscribedEvent(

        Long newsletterId,
        LocalDate birthDate
) {

    public static MaeilMailSubscribedEvent of(Long newsletterId, LocalDate birthDate) {
        return new MaeilMailSubscribedEvent(newsletterId, birthDate);
    }
}
