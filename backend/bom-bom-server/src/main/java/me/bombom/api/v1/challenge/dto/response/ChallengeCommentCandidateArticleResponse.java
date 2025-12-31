package me.bombom.api.v1.challenge.dto.response;

import jakarta.validation.constraints.NotNull;

public record ChallengeCommentCandidateArticleResponse(

        @NotNull
        Long articleId,

        @NotNull
        String newsletterName,

        @NotNull
        String articleTitle
) {
}
