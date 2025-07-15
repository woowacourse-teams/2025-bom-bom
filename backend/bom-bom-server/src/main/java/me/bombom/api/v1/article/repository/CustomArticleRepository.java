package me.bombom.api.v1.article.repository;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.enums.SortOption;

public interface CustomArticleRepository { //TODO:이름 고민
    List<ArticleResponse> findByMemberId(Long memberId, LocalDate date, Long categoryId, SortOption sortOption);
}