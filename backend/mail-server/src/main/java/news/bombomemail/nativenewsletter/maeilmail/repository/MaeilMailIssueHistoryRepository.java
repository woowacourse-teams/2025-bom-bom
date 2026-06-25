package news.bombomemail.nativenewsletter.maeilmail.repository;

import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailIssueHistoryRepository
        extends JpaRepository<MaeilMailIssueHistory, Long>, CustomMaeilMailIssueHistoryRepository {
}
