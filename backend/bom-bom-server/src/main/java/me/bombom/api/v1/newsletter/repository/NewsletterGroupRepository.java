package me.bombom.api.v1.newsletter.repository;

import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterGroupRepository extends JpaRepository<NewsletterGroup, Long> {
}
