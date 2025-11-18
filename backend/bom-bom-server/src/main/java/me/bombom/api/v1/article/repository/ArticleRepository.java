package me.bombom.api.v1.article.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.response.ArticleCountPerNewsletterResponse;
import me.bombom.api.v1.article.dto.response.PreviousArticleDetailResponse;
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
           OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
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
            a.id,
            me.bombom.api.v1.newsletter.domain.PreviousArticleSource.LATEST,
            a.title,
            a.contentsSummary,
            a.expectedReadTime
        )
        FROM Article a
        JOIN Newsletter n ON n.id = a.newsletterId
        WHERE a.newsletterId = :newsletterId
          AND a.memberId = :memberId
          AND a.arrivedDateTime < (
                SELECT MAX(a2.arrivedDateTime)
                FROM Article a2
                WHERE a2.newsletterId = :newsletterId AND a2.memberId = :memberId
            )
        ORDER BY a.arrivedDateTime DESC
        LIMIT :limit
    """)
    List<PreviousArticleResponse> findArticlesExceptLatestByMemberIdAndNewsletterId(
            @Param("newsletterId") Long newsletterId,
            @Param("memberId") Long memberId,
            @Param("limit") int limit
    );

    @Query("""
        SELECT new me.bombom.api.v1.article.dto.response.PreviousArticleDetailResponse(
            a.title,
            a.contents,
            a.arrivedDateTime,
            a.expectedReadTime,
            npp.exposureRatio,
            CASE
                WHEN :memberId IS NULL THEN false
                WHEN s.id IS NULL THEN false
                ELSE true
            END,
            new me.bombom.api.v1.newsletter.dto.NewsletterBasicResponse(
                n.name, n.email, n.imageUrl, c.name
            )
        )
        FROM Article a
        JOIN Newsletter n ON n.id = a.newsletterId
        JOIN Category c ON c.id = n.categoryId
        JOIN NewsletterPreviousPolicy npp ON npp.newsletterId = n.id
        LEFT JOIN Subscribe s ON s.newsletterId = n.id AND s.memberId = :memberId
        WHERE a.id = :id
        AND a.memberId = :adminId
        AND npp.strategy IN (
            me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy.FIXED_WITH_LATEST,
            me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy.LATEST_ONLY
        )
    """)
    Optional<PreviousArticleDetailResponse> getPreviousArticleDetailsByMemberId(
            @Param("id") Long id,
            @Param("adminId") Long adminId,
            @Param("memberId") Long memberId
    );

    @Modifying(clearAutomatically = true)
    @Query(value = """
    WITH cleanup_candidates AS (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY newsletter_id
                   ORDER BY arrived_date_time DESC, id DESC
               ) AS keep_order
        FROM article
        WHERE member_id = :memberId
    )
    DELETE FROM article
    WHERE id IN (
        SELECT id FROM cleanup_candidates
        WHERE keep_order > :keepCount
    )
    """, nativeQuery = true)
    int cleanupOldPreviousArticles(@Param("memberId") Long memberId, @Param("keepCount") int keepCount);

    long countByIdInAndMemberId(List<Long> ids, Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Article a WHERE a.id IN :ids AND a.memberId = :memberId")
    void deleteAllByIdsAndMemberId(@Param("ids") List<Long> ids, @Param("memberId") Long memberId);
}
