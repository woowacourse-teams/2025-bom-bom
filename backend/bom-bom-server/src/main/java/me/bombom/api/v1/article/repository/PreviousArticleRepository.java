package me.bombom.api.v1.article.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.article.domain.PreviousArticle;
import me.bombom.api.v1.article.dto.response.PreviousArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PreviousArticleRepository extends JpaRepository<PreviousArticle, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.article.dto.response.PreviousArticleResponse(
            pa.id,
            me.bombom.api.v1.newsletter.domain.PreviousArticleSource.FIXED,
            pa.title,
            pa.contentsSummary,
            pa.expectedReadTime
        )
        FROM PreviousArticle pa
        WHERE pa.newsletterId = :newsletterId
        ORDER BY pa.arrivedDateTime DESC
        LIMIT :limit
    """)
    List<PreviousArticleResponse> findByNewsletterId(@Param("newsletterId") Long newsletterId, @Param("limit") int limit);

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
        AND npp.strategy IN (
            me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy.FIXED_WITH_LATEST,
            me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy.FIXED_ONLY
        )
    """)
    Optional<PreviousArticleDetailResponse> findPreviousArticleDetailById(
            @Param("id") Long id,
            @Param("memberId") Long memberId
    );
}
