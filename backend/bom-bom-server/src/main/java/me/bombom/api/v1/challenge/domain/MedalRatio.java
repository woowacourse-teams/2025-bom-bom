package me.bombom.api.v1.challenge.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 수료한 챌린지의 등급별 비율(%). 세 값의 합은 항상 100(수료 0건이면 0/0/0)
 */
@Getter
@EqualsAndHashCode
public final class MedalRatio {

    public static final MedalRatio ZERO = new MedalRatio(0, 0, 0);

    private final int gold;
    private final int silver;
    private final int bronze;

    private MedalRatio(int gold, int silver, int bronze) {
        this.gold = gold;
        this.silver = silver;
        this.bronze = bronze;
    }

    /**
     * 등급별 개수로부터 비율(%) 계산. 반올림 잔차는 개수가 가장 많은 등급에 더해 합계 100을 맞춘다.
     */
    public static MedalRatio of(int goldCount, int silverCount, int bronzeCount) {
        int total = goldCount + silverCount + bronzeCount;
        if (total == 0) {
            return ZERO;
        }
        int gold = (int) Math.round(goldCount * 100.0 / total);
        int silver = (int) Math.round(silverCount * 100.0 / total);
        int bronze = (int) Math.round(bronzeCount * 100.0 / total);
        int remainder = 100 - (gold + silver + bronze);

        if (goldCount >= silverCount && goldCount >= bronzeCount) {
            return new MedalRatio(gold + remainder, silver, bronze);
        }
        if (silverCount >= bronzeCount) {
            return new MedalRatio(gold, silver + remainder, bronze);
        }
        return new MedalRatio(gold, silver, bronze + remainder);
    }
}
