package news.bombomemail.newsletter.repository;

import java.util.Optional;
import news.bombomemail.newsletter.domain.NewsletterVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterVerificationRepository extends JpaRepository<NewsletterVerification, Long> {

    Optional<NewsletterVerification> findByEmail(String email);
}
