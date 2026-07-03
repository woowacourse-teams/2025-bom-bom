package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.ChallengeGrade;
import me.bombom.api.v1.challenge.domain.CompletedChallengeSummary;

public record CompletedChallengeResponse(

        @Schema(description = "챌린지 식별자", example = "201")
        Long challengeId,

        @Schema(description = "챌린지 이름", example = "30일 독서 챌린지")
        String title,

        @Schema(description = "챌린지 시작일")
        LocalDate startDate,

        @Schema(description = "챌린지 종료일")
        LocalDate endDate,

        @Schema(description = "출석률 (%)", example = "92")
        int attendanceRate,

        @Schema(description = "수료 결과 등급", example = "GOLD")
        ChallengeGrade grade
) {

    public static CompletedChallengeResponse from(CompletedChallengeSummary summary) {
        return new CompletedChallengeResponse(
                summary.challengeId(),
                summary.title(),
                summary.startDate(),
                summary.endDate(),
                summary.attendanceRate(),
                summary.grade()
        );
    }
}
