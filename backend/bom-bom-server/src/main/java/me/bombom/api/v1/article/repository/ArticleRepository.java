package me.bombom.api.v1.article.repository;

import java.util.List;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.response.ArticleCountPerNewsletterResponse;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long>, CustomArticleRepository {

    @Query("""
    SELECT new me.bombom.api.v1.article.dto.response.ArticleCountPerNewsletterResponse(
        n.id, n.name, COALESCE(n.imageUrl, ''), CAST(COUNT(a.id) AS int)
    )
    FROM Article a
    JOIN Newsletter n ON n.id = a.newsletterId
    WHERE a.memberId = :memberId
      AND (:keyword IS NULL OR :keyword = ''
           OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.contentsSummary) LIKE LOWER(CONCAT('%', :keyword, '%')))
    GROUP BY n.id, n.name, n.imageUrl
    ORDER BY COUNT(a.id) DESC
    """)
    List<ArticleCountPerNewsletterResponse> countPerNewsletter(
            @Param("memberId") Long memberId,
            @Param("keyword") String keyword
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Article a WHERE a.memberId = :memberId")
    void deleteAllByMemberId(Long memberId);

    @Query("""
    SELECT new me.bombom.api.v1.article.dto.response.PreviousArticleResponse(
        a.id, a.title, a.contentsSummary, a.expectedReadTime
    )
    FROM Article a
    JOIN Newsletter n ON n.id = a.newsletterId
    WHERE a.memberId = :memberId
    ORDER BY a.arrivedDateTime DESC, a.id ASC
    LIMIT :limit
    
    """)
    List<PreviousArticleResponse> findArticlesByMemberIdAndNewsletterId(
            Long newsletterId,
            Long memberId,
            int limit
    );
}
