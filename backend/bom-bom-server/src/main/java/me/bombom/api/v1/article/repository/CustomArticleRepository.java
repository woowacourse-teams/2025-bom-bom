package me.bombom.api.v1.article.repository;

import java.time.LocalDate;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticlesOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomArticleRepository {

    Page<ArticleResponse> findByMemberId(Long memberId, GetArticlesOptions options, Pageable pageable);
    int countAllByMemberId(Long memberId, String keyword);
    int countAllByCategoryIdAndMemberId(Long memberId, Long categoryId, String keyword);
    int countByMemberIdAndArrivedDateTimeAndIsRead(Long memberId, LocalDate date, boolean isRead);
}
