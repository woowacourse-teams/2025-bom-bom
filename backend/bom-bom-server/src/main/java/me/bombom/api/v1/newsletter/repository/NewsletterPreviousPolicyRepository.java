package me.bombom.api.v1.newsletter.repository;

import me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterPreviousPolicyRepository extends JpaRepository<NewsletterPreviousPolicy, Long> {

    NewsletterPreviousPolicy findByNewsletterId(Long newsletterId);
}
