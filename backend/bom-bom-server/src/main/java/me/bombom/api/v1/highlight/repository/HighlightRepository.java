package me.bombom.api.v1.highlight.repository;

import java.util.Optional;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.domain.HighlightLocation;
import me.bombom.api.v1.highlight.dto.response.HighlightResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HighlightRepository extends JpaRepository<Highlight, Long>, CustomHighlightRepository {

    Optional<Highlight> findByArticleIdAndHighlightLocation(Long articleId, HighlightLocation highlightLocation);

    @Query("""
        SELECT new me.bombom.api.v1.highlight.dto.response.HighlightResponse(
            h.id,
            new me.bombom.api.v1.highlight.domain.HighlightLocation(
                h.highlightLocation.startOffset,
                h.highlightLocation.startXPath,
                h.highlightLocation.endOffset,
                h.highlightLocation.endXPath
            ),
            h.articleId,
            h.color.value,
            h.text,
            h.memo
        )
        FROM Highlight h
        JOIN Article a ON h.articleId = a.id
        WHERE a.memberId = :memberId
    """)
    List<HighlightResponse> findByMemberId(Long memberId);
}
