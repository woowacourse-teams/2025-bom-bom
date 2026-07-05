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
 * 수료한 챌린지의 등급별 "개수"(금/은/동) - 등급 판정/집계
 */
@Getter
@EqualsAndHashCode
public final class MedalCounts {

    public static final MedalCounts ZERO = new MedalCounts(0, 0, 0);

    private final int gold;
    private final int silver;
    private final int bronze;

    private MedalCounts(int gold, int silver, int bronze) {
        this.gold = gold;
        this.silver = silver;
        this.bronze = bronze;
    }

    /**
     * 한 회원의 종료 챌린지 참여 기록을 수료 등급({@link ChallengeGrade})으로 분류해 등급별 개수를 센다.
     */
    public static MedalCounts from(List<MemberMedalParticipationFlat> participations) {
        Map<BadgeGrade, Long> countByGrade = participations.stream()
                .map(participation -> ChallengeGrade.calculate(
                        calculateProgress(participation.attendedDays(), participation.totalDays()),
                        participation.isSurvived()
                ))
                .map(ChallengeGrade::toBadge)
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(grade -> grade, Collectors.counting()));

        return new MedalCounts(
                countByGrade.getOrDefault(BadgeGrade.GOLD, 0L).intValue(),
                countByGrade.getOrDefault(BadgeGrade.SILVER, 0L).intValue(),
                countByGrade.getOrDefault(BadgeGrade.BRONZE, 0L).intValue()
        );
    }

    public int total() {
        return gold + silver + bronze;
    }

    private static int calculateProgress(int attendedDays, int totalDays) {
        if (totalDays <= 0) {
            return 0;
        }
        return Math.min((int) ((double) attendedDays / totalDays * 100), 100);
    }
}
