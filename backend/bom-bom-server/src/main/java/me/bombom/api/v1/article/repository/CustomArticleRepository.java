package me.bombom.api.v1.article.repository;

import java.time.LocalDate;
import me.bombom.api.v1.article.dto.response.ArticleResponse;
import me.bombom.api.v1.article.dto.request.ArticlesOptionsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomArticleRepository {

    Page<ArticleResponse> findArticles(Long memberId, ArticlesOptionsRequest options, Pageable pageable);

    int countByMemberIdAndArrivedDateTimeAndIsRead(Long memberId, LocalDate date, boolean isRead);
}
