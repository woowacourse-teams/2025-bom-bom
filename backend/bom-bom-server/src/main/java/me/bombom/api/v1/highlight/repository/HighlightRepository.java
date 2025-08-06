package me.bombom.api.v1.highlight.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.domain.HighlightLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HighlightRepository extends JpaRepository<Highlight, Long> {

    List<Highlight> findByArticleId(Long articleId);
    Optional<Highlight> findByArticleIdAndHighlightLocation(Long articleId, HighlightLocation highlightLocation);
}
