package me.bombom.api.v1.newsletterrequest.repository;

import java.util.Optional;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestDraft;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterRequestDraftRepository extends JpaRepository<NewsletterRequestDraft, Long> {

    Optional<NewsletterRequestDraft> findByNewsletterRequestId(Long newsletterRequestId);
}
