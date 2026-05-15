package news.bombomemail.subscribe.repository;

import news.bombomemail.subscribe.domain.UnsubscribePattern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnsubscribePatternRepository extends JpaRepository<UnsubscribePattern, Long> {
}
