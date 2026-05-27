package me.bombom.api.v1.challenge.dto.response;

public record ChallengeReviewListItem(
        Long reviewId,
        String nickname,
        String comment,
        boolean isPrivate,
        Long memberId
) {
}
