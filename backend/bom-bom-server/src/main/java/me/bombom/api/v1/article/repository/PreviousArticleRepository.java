package me.bombom.api.v1.article.repository;

import java.util.List;
import me.bombom.api.v1.article.domain.PreviousArticle;
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
}
