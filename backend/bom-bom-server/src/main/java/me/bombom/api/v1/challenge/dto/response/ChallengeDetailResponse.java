package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import me.bombom.api.v1.challenge.domain.ChallengeGrade;

public record ChallengeDetailResponse(

        @Schema(type = "boolean", description = "참여 여부", required = true)
        boolean isJoined,

        @Schema(type = "integer", format = "int32", description = "진행률(%)", required = true)
        int progress,

        ChallengeGrade grade,

        Boolean isSuccess
) {

    public static ChallengeDetailResponse notJoined() {
        return new ChallengeDetailResponse(false, 0, null, null);
    }

    public static ChallengeDetailResponse ongoing(int progress) {
        return new ChallengeDetailResponse(true, progress, null, null);
    }

    public static ChallengeDetailResponse ended(int progress, boolean isSurvived) {
        ChallengeGrade grade = ChallengeGrade.calculate(progress, isSurvived);
        boolean isSuccess = grade != ChallengeGrade.FAIL && grade != ChallengeGrade.NONE;
        return new ChallengeDetailResponse(true, progress, grade, isSuccess);
    }
}
