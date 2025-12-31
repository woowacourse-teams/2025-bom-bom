package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.dto.TeamChallengeProgressFlat;

public record TeamChallengeProgressResponse(

        @NotNull
        ChallengeSummaryResponse challengeSummaryResponse,

        @Schema(required = true)
        int achievementAverage,

        @NotNull
        List<MemberDailyResultResponse> members
) {

    public static TeamChallengeProgressResponse of(
            Challenge challenge,
            List<TeamChallengeProgressFlat> progressList
    ) {
        return new TeamChallengeProgressResponse(
                ChallengeSummaryResponse.from(challenge),
                progressList.getFirst().teamProgress(),
                MemberDailyResultResponse.from(progressList)
        );
    }
}
