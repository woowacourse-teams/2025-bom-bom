package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ChallengeTeamListResponse(

        @Schema(required = true)
        int totalTeamCount,

        Long myTeamId,

        @NotNull
        List<TeamInfoResponse> teams
) {
    public record TeamInfoResponse(

            @NotNull
            Long teamId,

            @Schema(required = true)
            int teamNumber,

            @Schema(required = true)
            boolean isMyTeam
    ) {
    }
}
