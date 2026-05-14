package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeGrade;
import me.bombom.api.v1.member.domain.Member;

public record CertificationInfoResponse(

        @NotNull
        String nickname,

        @NotNull
        String challengeName,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        int generation,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotNull
        ChallengeGrade medal,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        int medalCondition
) {

    public static CertificationInfoResponse of(Member member, Challenge challenge, ChallengeGrade challengeGrade) {
        return new CertificationInfoResponse(
                member.getNickname(),
                challenge.getName(),
                challenge.getGeneration(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challengeGrade,
                challengeGrade.getMinProgress()
        );
    }
}
