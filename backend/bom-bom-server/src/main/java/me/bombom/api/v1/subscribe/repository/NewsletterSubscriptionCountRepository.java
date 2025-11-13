package me.bombom.api.v1.subscribe.repository;

import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriptionCountRepository extends JpaRepository<NewsletterSubscriptionCount, Long> {
}
