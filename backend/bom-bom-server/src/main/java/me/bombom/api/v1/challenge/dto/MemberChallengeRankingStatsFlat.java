package me.bombom.api.v1.challenge.dto;

/**
 * 회원별 종료 챌린지 집계(SQL GROUP BY, 회원당 1행).
 * 완료수·완료율·평균출석률과 상대 순위(topPercent) 산출에 사용한다.
 */
public record MemberChallengeRankingStatsFlat(
        Long memberId,
        Long participatedCount,
        Long survivedCount,
        Double averageAttendance
) {
}
