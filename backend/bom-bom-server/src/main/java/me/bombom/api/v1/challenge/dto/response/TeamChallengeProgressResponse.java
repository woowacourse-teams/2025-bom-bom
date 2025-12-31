package me.bombom.api.v1.challenge.dto.response;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.dto.TeamChallengeProgressFlat;

public record TeamChallengeProgressResponse(

        @NotNull
        ChallengeSummaryResponse challenge,

        @NotNull
        TeamSummaryResponse teamSummary,

        @NotNull
        List<MemberDailyResultResponse> members
) {

    public static TeamChallengeProgressResponse of(
            Challenge challenge,
            List<TeamChallengeProgressFlat> progressList
    ) {

        TeamChallengeProgressFlat representative = progressList.getFirst();
        return new TeamChallengeProgressResponse(
                ChallengeSummaryResponse.from(challenge),
                TeamSummaryResponse.from(representative),
                MemberDailyResultResponse.from(progressList)
        );
    }
}
