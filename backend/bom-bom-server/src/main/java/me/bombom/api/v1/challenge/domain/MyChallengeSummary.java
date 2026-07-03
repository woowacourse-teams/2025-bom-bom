package me.bombom.api.v1.challenge.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.bombom.api.v1.challenge.dto.MemberChallengeRankingStatsFlat;
import me.bombom.api.v1.challenge.dto.MemberMedalParticipationFlat;

public record MyChallengeSummary(
        int completedChallengeCount,
        double completionTopPercent,
        int completionRate,
        double attendanceTopPercent,
        int averageAttendanceRate,
        MedalRatio medalRatio
) {

    public static final MyChallengeSummary EMPTY = new MyChallengeSummary(0, 0.0, 0, 0.0, 0, MedalRatio.ZERO);

    public static MyChallengeSummary of(
            List<MemberChallengeRankingStatsFlat> aggregates,
            List<MemberMedalParticipationFlat> myParticipations,
            Long memberId
    ) {
        Map<Long, MemberChallengeStats> statsByMember = aggregates.stream()
                .collect(Collectors.toMap(
                        MemberChallengeRankingStatsFlat::memberId,
                        MemberChallengeStats::from
                ));
        if (!statsByMember.containsKey(memberId)) {
            return EMPTY;
        }

        return from(statsByMember.get(memberId), statsByMember.values(), MedalRatio.from(myParticipations));
    }

    private static MyChallengeSummary from(
            MemberChallengeStats myStats,
            Collection<MemberChallengeStats> population,
            MedalRatio medalRatio
    ) {
        double completionTopPercent = topPercent(
                myStats.getCompletionRate(),
                population.stream().map(MemberChallengeStats::getCompletionRate).toList()
        );
        double attendanceTopPercent = topPercent(
                myStats.getAverageAttendanceRate(),
                population.stream().map(MemberChallengeStats::getAverageAttendanceRate).toList()
        );

        return new MyChallengeSummary(
                myStats.getCompletedChallengeCount(),
                completionTopPercent,
                myStats.getCompletionRate(),
                attendanceTopPercent,
                myStats.getAverageAttendanceRate(),
                medalRatio
        );
    }

    /**
     * 모집단에서 내 점수보다 높은 회원의 비율(상위 %). 소수 첫째자리까지 반올림
     */
    private static double topPercent(int myScore, List<Integer> populationScores) {
        if (populationScores.isEmpty()) {
            return 0.0;
        }
        long higherCount = populationScores.stream()
                .filter(score -> score > myScore)
                .count();
        double percent = higherCount * 100.0 / populationScores.size();
        return Math.round(percent * 10) / 10.0;
    }
}
