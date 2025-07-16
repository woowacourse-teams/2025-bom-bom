package me.bombom.api.v1.article.repository;

import java.time.LocalDate;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.enums.SortOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomArticleRepository { //TODO:이름 고민
    Page<ArticleResponse> findByMemberId(Long memberId, LocalDate date, Long categoryId, SortOption sortOption, Pageable pageable);
}