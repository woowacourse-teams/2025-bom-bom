package me.bombom.api.v1.reading.event;

public record ContinueReadingIncreasedEvent(
        Long memberId,
        int streakDayCount
) {
}
