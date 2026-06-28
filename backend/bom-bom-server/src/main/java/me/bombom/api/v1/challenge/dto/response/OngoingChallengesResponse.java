package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.challenge.domain.OngoingChallengeSummary;

@Schema(description = "마이페이지 참여 중 챌린지 목록")
public record OngoingChallengesResponse(

        @NotNull
        @Schema(description = "참여 중 챌린지 목록 (없으면 빈 배열)")
        List<OngoingChallengeResponse> challenges
) {

    public static OngoingChallengesResponse from(List<OngoingChallengeSummary> summaries) {
        return new OngoingChallengesResponse(
                summaries.stream()
                        .map(OngoingChallengeResponse::from)
                        .toList()
        );
    }
}
