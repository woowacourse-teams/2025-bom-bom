package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;

public record ChallengeReviewResponse(

        @NotNull
        @Schema(description = "리뷰 코멘트 식별자", example = "1")
        Long reviewId,

        @NotNull
        @Schema(description = "작성자 별명", example = "나밍곰")
        String nickname,

        @NotNull
        @Schema(description = "작성자 코멘트", example = "좋았어요")
        String comment,

        @Schema(
                requiredMode = RequiredMode.REQUIRED,
                description = "비밀글 여부",
                example = "false"
        )
        boolean isPrivate
) {

    public static final String WITHDRAWN_MEMBER_NICKNAME = "탈퇴한 사용자";

    public static ChallengeReviewResponse of(ChallengeReviewListItem item) {
        String displayNickname = item.nickname() != null ? item.nickname() : WITHDRAWN_MEMBER_NICKNAME;
        return new ChallengeReviewResponse(
                item.reviewId(),
                displayNickname,
                item.comment(),
                item.isPrivate()
        );
    }
}
