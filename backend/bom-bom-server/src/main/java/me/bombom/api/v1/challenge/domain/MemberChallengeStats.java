package me.bombom.api.v1.challenge.domain;

import lombok.Getter;
import me.bombom.api.v1.challenge.dto.MemberChallengeRankingStatsFlat;

/**
 * 한 회원의 종료된 챌린지 참여 기록으로부터 마이페이지 요약 통계를 계산한 값 객체.
 * 참여한 종료 챌린지가 없으면 모든 값이 0이다.
 */
@Getter
public class MemberChallengeStats {

    private final int completedChallengeCount;
    private final int completionRate;
    private final int averageAttendanceRate;

    private MemberChallengeStats(
            int completedChallengeCount,
            int completionRate,
            int averageAttendanceRate
    ) {
        this.completedChallengeCount = completedChallengeCount;
        this.completionRate = completionRate;
        this.averageAttendanceRate = averageAttendanceRate;
    }

    public static MemberChallengeStats from(MemberChallengeRankingStatsFlat aggregate) {
        int participatedCount = aggregate.participatedCount().intValue();
        int survivedCount = aggregate.survivedCount().intValue();

        int completionRate = 0;
        if (participatedCount > 0) {
            completionRate = Math.round(survivedCount * 100.0f / participatedCount);
        }
        int averageAttendanceRate = 0;
        if (aggregate.averageAttendance() != null) {
            averageAttendanceRate = Math.round(aggregate.averageAttendance().floatValue());
        }
        return new MemberChallengeStats(survivedCount, completionRate, averageAttendanceRate);
    }
}
