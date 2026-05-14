package me.bombom.api.v1.article.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.article.domain.PreviousArticle;
import me.bombom.api.v1.article.dto.response.PreviousArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PreviousArticleRepository extends JpaRepository<PreviousArticle, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.article.dto.response.PreviousArticleResponse(
            pa.id,
            pa.title,
            pa.contentsSummary,
            pa.expectedReadTime
        )
        FROM PreviousArticle pa
        WHERE pa.newsletterId = :newsletterId
        AND pa.isFixed = true
        ORDER BY pa.arrivedDateTime DESC
        LIMIT :limit
    """)
    List<PreviousArticleResponse> findByNewsletterIdAndFixed(
            @Param("newsletterId") Long newsletterId,
            int limit
    );

    @Query("""
        SELECT new me.bombom.api.v1.article.dto.response.PreviousArticleDetailResponse(
            pa.title,
            pa.contents,
            pa.arrivedDateTime,
            pa.expectedReadTime,
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
        FROM PreviousArticle pa
        JOIN Newsletter n ON n.id = pa.newsletterId
        JOIN Category c ON c.id = n.categoryId
        JOIN NewsletterPreviousPolicy npp ON npp.newsletterId = n.id
        LEFT JOIN Subscribe s ON s.newsletterId = n.id AND s.memberId = :memberId
        WHERE pa.id = :id
        AND npp.strategy != me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy.INACTIVE
    """)
    Optional<PreviousArticleDetailResponse> findPreviousArticleDetailById(
            @Param("id") Long id,
            @Param("memberId") Long memberId
    );

    /**
     * 자동 이동된 아티클 중 최신 제외하고 조회
     * - isFixed=false (자동 이동)
     * - article의 최신보다 오래된 것만
     */
    @Query("""
        SELECT new me.bombom.api.v1.article.dto.response.PreviousArticleResponse(
            pa.id,
            pa.title,
            pa.contentsSummary,
            pa.expectedReadTime
        )
        FROM PreviousArticle pa
        WHERE pa.newsletterId = :newsletterId
          AND pa.arrivedDateTime < (
                SELECT MAX(pa2.arrivedDateTime)
                FROM PreviousArticle pa2
                WHERE pa2.newsletterId = :newsletterId
                  AND pa2.isFixed = false
            )
          AND pa.isFixed = false
        ORDER BY pa.arrivedDateTime DESC
        LIMIT :limit
    """)
    List<PreviousArticleResponse> findExceptLatestByNewsletterId(@Param("newsletterId") Long newsletterId, @Param("limit") int limit);

    /**
     * previous_article 테이블에서 오래된 자동 이동 아티클 정리
     * - isFixed = false인 것만 대상 (직접 지정한 고정 아티클은 유지)
     * - 뉴스레터별로 최신 N개만 유지
     */
    @Modifying(clearAutomatically = true)
    @Query(value = """
        WITH cleanup_candidates AS (
            SELECT id,
                   ROW_NUMBER() OVER (
                       PARTITION BY newsletter_id
                       ORDER BY arrived_date_time DESC, id DESC
                   ) AS keep_order
            FROM previous_article
            WHERE is_fixed = false
        )
        DELETE FROM previous_article
        WHERE id IN (
            SELECT id FROM cleanup_candidates
            WHERE keep_order > :keepCount
        )
    """, nativeQuery = true)
    int cleanupOldPreviousArticles(@Param("keepCount") int keepCount);
}
