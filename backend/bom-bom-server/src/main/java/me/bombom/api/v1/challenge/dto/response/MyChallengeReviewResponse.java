package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.challenge.domain.ChallengeReview;

public record MyChallengeReviewResponse(

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
                description = "비밀글 여부 (자신이 쓴 글이 비공개인지 표시할 때 사용)",
                example = "false"
        )
        boolean isPrivate
) {

    public static MyChallengeReviewResponse of(ChallengeReview review, String viewerNickname) {
        return new MyChallengeReviewResponse(
                review.getId(),
                viewerNickname,
                review.getComment(),
                review.isPrivate()
        );
    }
}
