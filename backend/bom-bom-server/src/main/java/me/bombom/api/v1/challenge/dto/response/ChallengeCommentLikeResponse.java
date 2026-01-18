package me.bombom.api.v1.challenge.dto.response;

import me.bombom.api.v1.challenge.domain.ChallengeComment;

public record ChallengeCommentLikeResponse(int likeCount) {

    public static ChallengeCommentLikeResponse from(ChallengeComment comment) {
        return new ChallengeCommentLikeResponse(comment.getLikeCount());
    }
}
