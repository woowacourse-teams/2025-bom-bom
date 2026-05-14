package news.bombomemail.newsletter.repository;

import java.util.Optional;
import news.bombomemail.newsletter.domain.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {

    Optional<Newsletter> findByEmail(String email);
}
