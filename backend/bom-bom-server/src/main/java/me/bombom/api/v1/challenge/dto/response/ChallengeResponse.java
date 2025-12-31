package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeStatus;

public record ChallengeResponse(

        @NotNull
        Long id,

        @NotNull
        String title,

        @NotNull
        int generation,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @Schema(type = "integer", format = "int64", description = "참가자 수", required = true)
        long participantCount,

        @NotNull List<ChallengeNewsletterResponse>
        newsletters,

        @NotNull
        ChallengeStatus status,

        ChallengeDetailResponse detail
) {

    public static ChallengeResponse of(
            Challenge challenge,
            long participantCount,
            List<ChallengeNewsletterResponse> newsletters,
            ChallengeStatus status,
            ChallengeDetailResponse detail
    ) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getName(),
                challenge.getGeneration(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                participantCount,
                newsletters,
                status,
                detail
        );
    }
}
