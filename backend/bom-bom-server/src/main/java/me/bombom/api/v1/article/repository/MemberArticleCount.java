package me.bombom.api.v1.article.repository;

public record MemberArticleCount(

        Long memberId,
        Long roleId,
        long unbookmarkedArticleCount
) {
}
