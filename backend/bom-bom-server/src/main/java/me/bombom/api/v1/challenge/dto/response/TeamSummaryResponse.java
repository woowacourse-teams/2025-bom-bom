package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import me.bombom.api.v1.challenge.dto.TeamChallengeProgressFlat;

public record TeamSummaryResponse(

        @Schema(required = true)
        int achievementAverage
) {

    public static TeamSummaryResponse from(TeamChallengeProgressFlat representative) {
        return new TeamSummaryResponse(representative.teamProgress());
    }
}
