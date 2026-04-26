package news.bombomemail.nativenewsletter.maeilmail.repository;

import java.time.LocalDate;
import java.util.Optional;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailIssueJobRepository extends JpaRepository<MaeilMailIssueJob, Long> {

    Optional<MaeilMailIssueJob> findByIssueDate(LocalDate issueDate);
}
