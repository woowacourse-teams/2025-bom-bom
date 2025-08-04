package me.bombom.api.v1.bookmark.dto;

import com.querydsl.core.annotations.QueryProjection;
import me.bombom.api.v1.article.dto.ArticleResponse;

public record BookmarkResponse(Long id, ArticleResponse articleResponse) {

    @QueryProjection
    public BookmarkResponse {}
}
