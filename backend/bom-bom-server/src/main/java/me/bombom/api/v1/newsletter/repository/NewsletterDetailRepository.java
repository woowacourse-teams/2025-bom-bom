package me.bombom.api.v1.newsletter.repository;

import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterDetailRepository extends JpaRepository<NewsletterDetail, Long> {
}
