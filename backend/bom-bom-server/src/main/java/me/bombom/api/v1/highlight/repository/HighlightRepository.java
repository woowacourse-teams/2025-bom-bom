package me.bombom.api.v1.highlight.repository;

import java.util.List;
import me.bombom.api.v1.highlight.domain.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HighlightRepository extends JpaRepository<Highlight, Long> {
    List<Highlight> findByArticleId(Long articleId);
}
