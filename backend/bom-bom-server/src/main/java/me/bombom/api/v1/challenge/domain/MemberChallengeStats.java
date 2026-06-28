package me.bombom.api.v1.challenge.domain;

import java.util.List;
import lombok.Getter;
import me.bombom.api.v1.challenge.dto.EndedChallengeParticipationFlat;

/**
 * 한 회원의 종료된 챌린지 참여 기록으로부터 마이페이지 요약 통계를 계산한 값 객체.
 * 참여한 종료 챌린지가 없으면 모든 값이 0이다.
 */
@Getter
public class MemberChallengeStats {

    public static final MemberChallengeStats EMPTY =
            new MemberChallengeStats(0, 0, 0, MedalRatio.ZERO);

    private final int completedChallengeCount;
    private final int completionRate;
    private final int averageAttendanceRate;
    private final MedalRatio medalRatio;

    private MemberChallengeStats(
            int completedChallengeCount,
            int completionRate,
            int averageAttendanceRate,
            MedalRatio medalRatio
    ) {
        this.completedChallengeCount = completedChallengeCount;
        this.completionRate = completionRate;
        this.averageAttendanceRate = averageAttendanceRate;
        this.medalRatio = medalRatio;
    }

    public static MemberChallengeStats from(List<EndedChallengeParticipationFlat> participations) {
        int participatedCount = participations.size();
        if (participatedCount == 0) {
            return EMPTY;
        }

        ParticipationAggregate aggregate = aggregate(participations);
        int completedChallengeCount = aggregate.survivedCount();
        int completionRate = Math.round(completedChallengeCount * 100.0f / participatedCount);
        int averageAttendanceRate = Math.round(aggregate.attendanceSum() * 1.0f / participatedCount);
        MedalRatio medalRatio = MedalRatio.of(aggregate.gold(), aggregate.silver(), aggregate.bronze());
        return new MemberChallengeStats(completedChallengeCount, completionRate, averageAttendanceRate, medalRatio);
    }

    private static ParticipationAggregate aggregate(List<EndedChallengeParticipationFlat> participations) {
        int attendanceSum = 0;
        int survivedCount = 0;
        int gold = 0;
        int silver = 0;
        int bronze = 0;
        for (EndedChallengeParticipationFlat participation : participations) {
            int progress = calculateProgress(participation);
            attendanceSum += progress;
            if (participation.isSurvived()) {
                survivedCount++;
            }
            switch (ChallengeGrade.calculate(progress, participation.isSurvived())) {
                case GOLD -> gold++;
                case SILVER -> silver++;
                case BRONZE -> bronze++;
                case FAIL -> {
                }
            }
        }
        return new ParticipationAggregate(attendanceSum, survivedCount, gold, silver, bronze);
    }

    private static int calculateProgress(EndedChallengeParticipationFlat participation) {
        if (participation.totalDays() <= 0) {
            return 0;
        }
        int progress = (int) ((double) participation.attendedDays() / participation.totalDays() * 100);
        return Math.min(progress, 100);
    }

    private record ParticipationAggregate(int attendanceSum, int survivedCount, int gold, int silver, int bronze) {
    }
}
