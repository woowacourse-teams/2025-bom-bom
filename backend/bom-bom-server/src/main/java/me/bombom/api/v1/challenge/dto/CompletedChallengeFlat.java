package me.bombom.api.v1.challenge.dto;

import java.time.LocalDate;

/**
 * 회원이 참여한 종료 챌린지 단건(참여당 1행).
 * 챌린지 정보 + 본인 출석/생존 정보를 담아, 출석률·수료 등급({@code ChallengeGrade}) 계산에 사용한다.
 */
public record CompletedChallengeFlat(
        Long challengeId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        int completedDays,
        int totalDays,
        boolean isSurvived
) {
}
