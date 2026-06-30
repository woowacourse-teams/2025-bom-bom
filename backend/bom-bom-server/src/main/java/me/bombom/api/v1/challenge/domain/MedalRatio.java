package me.bombom.api.v1.challenge.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.challenge.dto.MemberMedalParticipationFlat;

/**
 * 수료한 챌린지의 등급별 비율(%). 소수점 버림이라 세 값의 합은 100 이하 가능(수료 0건이면 0/0/0)
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
     * 한 회원의 종료 챌린지 참여 기록을 수료 등급({@link ChallengeGrade})으로 분류해 메달 비율을 계산한다.
     */
    public static MedalRatio from(List<MemberMedalParticipationFlat> participations) {
        Map<BadgeGrade, Long> countByGrade = participations.stream()
                .map(participation -> ChallengeGrade.calculate(
                        calculateProgress(participation.attendedDays(), participation.totalDays()),
                        participation.isSurvived()
                ))
                .map(ChallengeGrade::toBadge)
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(grade -> grade, Collectors.counting()));

        return of(
                countByGrade.getOrDefault(BadgeGrade.GOLD, 0L).intValue(),
                countByGrade.getOrDefault(BadgeGrade.SILVER, 0L).intValue(),
                countByGrade.getOrDefault(BadgeGrade.BRONZE, 0L).intValue()
        );
    }

    private static int calculateProgress(int attendedDays, int totalDays) {
        if (totalDays <= 0) {
            return 0;
        }
        return Math.min((int) ((double) attendedDays / totalDays * 100), 100);
    }

    /**
     * 등급별 개수로 비율(%) 계산 - 소수점 버림
     */
    public static MedalRatio of(int goldCount, int silverCount, int bronzeCount) {
        int total = goldCount + silverCount + bronzeCount;
        if (total == 0) {
            return ZERO;
        }
        return new MedalRatio(
                goldCount * 100 / total,
                silverCount * 100 / total,
                bronzeCount * 100 / total
        );
    }
}
