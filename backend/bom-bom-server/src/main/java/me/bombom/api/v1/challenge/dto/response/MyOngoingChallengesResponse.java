package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.challenge.domain.OngoingChallengeSummary;

public record MyOngoingChallengesResponse(

        @NotNull
        @Schema(description = "참여 중 챌린지 목록 (없으면 빈 배열)")
        List<MyOngoingChallengeResponse> challenges
) {

    public static MyOngoingChallengesResponse from(List<OngoingChallengeSummary> summaries) {
        return new MyOngoingChallengesResponse(
                summaries.stream()
                        .map(MyOngoingChallengeResponse::from)
                        .toList()
        );
    }
}
