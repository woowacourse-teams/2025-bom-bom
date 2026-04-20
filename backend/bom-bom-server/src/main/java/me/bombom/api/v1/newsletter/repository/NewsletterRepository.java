package me.bombom.api.v1.newsletter.repository;

import java.util.Optional;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsletterRepository extends JpaRepository<Newsletter, Long>, CustomNewsletterRepository {

    @Query("""
        SELECT new me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse(
            n.name,
            n.description,
            n.imageUrl,
            c.name,
            n.status,
            n.source,
            d.mainPageUrl,
            d.subscribeUrl,
            d.issueCycle,
            d.previousNewsletterUrl,
            d.subscribeMethod,
            CASE
                WHEN :memberId IS NULL THEN false
                WHEN s.id IS NULL THEN false
                ELSE true
            END
        )
        FROM Newsletter n
        JOIN NewsletterDetail d ON d.id = n.detailId
        JOIN Category c ON c.id = n.categoryId
        LEFT JOIN Subscribe s ON s.newsletterId = n.id AND s.memberId = :memberId
        WHERE n.id = :newsletterId
    """)
    Optional<NewsletterWithDetailResponse> findNewsletterWithDetailById(
            @Param("newsletterId") Long newsletterId,
            @Param("memberId") Long memberId
    );
}
