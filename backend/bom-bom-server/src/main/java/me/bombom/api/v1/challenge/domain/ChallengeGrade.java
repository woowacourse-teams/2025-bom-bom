package me.bombom.api.v1.challenge.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.badge.domain.BadgeGrade;

@Getter
@RequiredArgsConstructor
public enum ChallengeGrade {

    GOLD("금메달", 100, BadgeGrade.GOLD),
    SILVER("은메달", 90, BadgeGrade.SILVER),
    BRONZE("동메달", 80, BadgeGrade.BRONZE),
    FAIL("탈락", 0, null),
    ;

    private final String description;
    private final int minProgress;
    private final BadgeGrade badgeGrade;

    public static ChallengeGrade calculate(int progress, boolean isSurvived) {
        if (!isSurvived) {
            return FAIL;
        }
        if (progress >= GOLD.minProgress) {
            return GOLD;
        }
        if (progress >= SILVER.minProgress) {
            return SILVER;
        }
        if (progress >= BRONZE.minProgress) {
            return BRONZE;
        }
        return FAIL;
    }

    public BadgeGrade toBadge() {
        return this.badgeGrade;
    }
}
