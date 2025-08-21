package news.bombomemail.subscribe.repository;

import java.util.Optional;
import news.bombomemail.subscribe.domain.NewsletterSubscriptionCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriptionCountRepository extends JpaRepository<NewsletterSubscriptionCount, Long> {

    Optional<NewsletterSubscriptionCount> findByNewsletterId(Long newsletterId);
}
