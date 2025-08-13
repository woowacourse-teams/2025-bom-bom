package me.bombom.api.v1.newsletter.repository;

import java.util.List;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.newsletter.dto.NewsletterResponse(
                n.id, n.name, n.imageUrl, n.description, d.subscribeUrl, c.name
            )
        FROM Newsletter n
        JOIN NewsletterDetail d ON n.detailId = d.id
        JOIN Category c ON c.id = n.categoryId
        ORDER BY d.subscribeCount DESC, n.name ASC
    """)
    List<NewsletterResponse> findNewslettersInfo();

    boolean existsByName(String name);
}
