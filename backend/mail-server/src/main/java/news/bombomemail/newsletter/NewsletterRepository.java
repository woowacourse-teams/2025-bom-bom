package news.bombomemail.newsletter;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {

    Optional<Newsletter> findByEmail(String email);
}
