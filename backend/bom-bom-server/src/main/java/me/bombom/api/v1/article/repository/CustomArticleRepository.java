package me.bombom.api.v1.article.repository;

import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticlesOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomArticleRepository {
    Page<ArticleResponse> findByMemberId(Long memberId, GetArticlesOptions options, Pageable pageable);
}
