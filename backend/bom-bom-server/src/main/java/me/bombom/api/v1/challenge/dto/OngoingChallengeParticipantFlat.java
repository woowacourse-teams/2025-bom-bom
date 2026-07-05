package me.bombom.api.v1.challenge.dto;

import java.time.LocalDate;

/**
 * 챌린지별 순위, 평균, 출석률 비교 계산에 사용
 */
public record OngoingChallengeParticipantFlat(
        Long challengeId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        int totalDays,
        Long challengeTeamId,
        Long memberId,
        int completedDays
) {
}
