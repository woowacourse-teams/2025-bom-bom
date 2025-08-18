package me.bombom.api.v1.highlight.repository;

import java.util.Optional;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.domain.HighlightLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HighlightRepository extends JpaRepository<Highlight, Long>, CustomHighlightRepository {

    Optional<Highlight> findByArticleIdAndHighlightLocation(Long articleId, HighlightLocation highlightLocation);

    @Query("""
        SELECT COUNT(*)
        FROM Highlight h
        JOIN Article a
        ON h.articleId = a.id
        WHERE a.memberId = :memberId
    """)
    int countByMemberId(Long memberId);

    int countByArticleId(Long articleId);
}
