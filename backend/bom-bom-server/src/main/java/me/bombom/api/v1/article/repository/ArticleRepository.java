package me.bombom.api.v1.article.repository;

import java.util.List;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.response.ArticleCountPerNewsletterResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long>, CustomArticleRepository {

    @Query("""
    SELECT new me.bombom.api.v1.article.dto.response.ArticleCountPerNewsletterResponse(
        n.id, n.name, coalesce(n.imageUrl, ''), cast(count(a.id) as int)
    )
    FROM Article a
    JOIN Newsletter n ON n.id = a.newsletterId
    WHERE a.memberId = :memberId
      AND (:keyword is null or :keyword = ''
           OR lower(a.title) LIKE lower(concat('%', :keyword, '%'))
           OR lower(a.contentsSummary) LIKE lower(concat('%', :keyword, '%')))
    GROUP BY n.id, n.name, n.imageUrl
    HAVING count(a.id) > 0
    ORDER BY count(a.id) DESC
    """)
    List<ArticleCountPerNewsletterResponse> countPerNewsletter(
            @Param("memberId") Long memberId,
            @Param("keyword") String keyword
    );
}
