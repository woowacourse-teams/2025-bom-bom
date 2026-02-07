package me.bombom.api.v1.newsletter.repository;

import java.util.List;
import me.bombom.api.v1.challenge.dto.ChallengeNewsletterRow;
import me.bombom.api.v1.newsletter.domain.NewsletterGroupItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsletterGroupItemRepository extends JpaRepository<NewsletterGroupItem, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.ChallengeNewsletterRow(
            c.id,
            n.id,
            n.name,
            n.imageUrl
        )
        FROM Challenge c
        JOIN NewsletterGroupItem ngi ON ngi.newsletterGroupId = c.newsletterGroupId
        JOIN Newsletter n ON n.id = ngi.newsletterId
        WHERE c.id IN :challengeIds
        ORDER BY n.name ASC
    """)
    List<ChallengeNewsletterRow> findNewsletterResponsesByChallengeIds(
            @Param("challengeIds") List<Long> challengeIds
    );

    @Query("""
        SELECT COUNT(1) > 0
        FROM Challenge c
        JOIN NewsletterGroupItem ngi ON ngi.newsletterGroupId = c.newsletterGroupId
        JOIN Newsletter n ON n.id = ngi.newsletterId
        JOIN Subscribe s ON s.newsletterId = n.id
        WHERE c.id = :challengeId
          AND s.memberId = :memberId
    """)
    boolean existsSubscribedNewsletter(@Param("challengeId") Long challengeId, @Param("memberId") Long memberId);
}
