package news.bombomemail.nativenewsletter.maeilmail.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueJob;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailIssueJobRepository extends JpaRepository<MaeilMailIssueJob, Long> {

    Optional<MaeilMailIssueJob> findByIssueDate(LocalDate issueDate);

    Optional<MaeilMailIssueJob> findByIssueDateAndStatusIn(
            LocalDate issueDate,
            Collection<MaeilMailIssueJobStatus> statuses
    );
}
