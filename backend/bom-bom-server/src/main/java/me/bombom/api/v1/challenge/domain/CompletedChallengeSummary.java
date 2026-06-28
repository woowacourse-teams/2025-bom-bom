package me.bombom.api.v1.challenge.domain;

import java.time.LocalDate;
import me.bombom.api.v1.challenge.dto.CompletedChallengeFlat;

/**
 * 종료 챌린지 1건의 마이페이지 항목 계산 결과. 수료 실패({@code FAIL})도 그대로 표현한다.
 */
public record CompletedChallengeSummary(
        Long challengeId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        int attendanceRate,
        ChallengeGrade grade
) {

    public CompletedChallengeSummary(CompletedChallengeFlat challenge, int attendanceRate, ChallengeGrade grade) {
        this(
                challenge.challengeId(),
                challenge.title(),
                challenge.startDate(),
                challenge.endDate(),
                attendanceRate,
                grade
        );
    }

    public static CompletedChallengeSummary of(CompletedChallengeFlat challenge) {
        int attendanceRate = calculateProgress(challenge.completedDays(), challenge.totalDays());
        ChallengeGrade grade = ChallengeGrade.calculate(attendanceRate, challenge.isSurvived());
        return new CompletedChallengeSummary(challenge, attendanceRate, grade);
    }

    private static int calculateProgress(int completedDays, int totalDays) {
        if (totalDays <= 0) {
            return 0;
        }
        return Math.min((int) ((double) completedDays / totalDays * 100), 100);
    }
}
