package me.bombom.api.v1.newsletter.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.newsletter.dto.NewsletterResponse(
            n.id,
            n.name,
            n.imageUrl,
            n.description,
            d.subscribeUrl,
            c.name,
            CASE
                WHEN :memberId IS NULL THEN false
                WHEN s.id IS NULL THEN false
                ELSE true
            END
        )
        FROM Newsletter n
        JOIN NewsletterDetail d ON d.id = n.detailId
        LEFT JOIN NewsletterSubscriptionCount nsc ON nsc.newsletterId = n.id
        JOIN Category c ON c.id = n.categoryId
        LEFT JOIN Subscribe s ON s.newsletterId = n.id AND s.memberId = :memberId
        ORDER BY COALESCE(nsc.total, 0) DESC, n.name ASC
    """)
    List<NewsletterResponse> findNewslettersInfo(@Param("memberId") Long memberId);

    @Query("""
        SELECT new me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse(
            n.name,
            n.description,
            n.imageUrl,
            c.name,
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
