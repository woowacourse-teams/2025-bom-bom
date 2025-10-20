package me.bombom.api.v1.highlight.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.domain.HighlightLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    List<Highlight> findAllByArticleId(Long articleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "DELETE h FROM highlight h "
                    + "JOIN article a ON h.article_id = a.id "
                    + "WHERE a.member_id = :memberId",
            nativeQuery = true
    )
    void deleteAllByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Highlight h 
        SET h.articleId = 0 
        WHERE h.articleId IN :articleIds
    """)
    void updateArticleDeleted(List<Long> articleIds);
}
