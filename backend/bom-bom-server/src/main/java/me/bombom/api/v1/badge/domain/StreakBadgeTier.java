package me.bombom.api.v1.badge.domain;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StreakBadgeTier {
    SEVEN(7),
    FIFTEEN(15),
    THIRTY(30),
    FIFTY(50),
    HUNDRED(100),
    ;

    private final int dayCount;

    public static Optional<StreakBadgeTier> fromStreakDayCount(int streakDayCount) {
        return Arrays.stream(values())
                .filter(tier -> tier.dayCount == streakDayCount)
                .findFirst();
    }
}
