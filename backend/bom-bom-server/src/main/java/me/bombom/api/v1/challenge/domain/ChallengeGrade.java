package me.bombom.api.v1.challenge.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengeGrade {

    GOLD("금메달", 100),
    SILVER("은메달", 80),
    BRONZE("동메달", 50),
    COMPLETE("완료", 0),
    FAIL("탈락", 0),
    ;

    private final String description;
    private final int minProgress;

    public static ChallengeGrade calculate(int progress, boolean isSurvived) {
        if (!isSurvived) return FAIL;
        if (progress >= GOLD.minProgress) return GOLD;
        if (progress >= SILVER.minProgress) return SILVER;
        if (progress >= BRONZE.minProgress) return BRONZE;
        return COMPLETE;
    }
}
