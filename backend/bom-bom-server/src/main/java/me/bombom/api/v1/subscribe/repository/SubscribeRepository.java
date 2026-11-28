package me.bombom.api.v1.subscribe.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.dto.response.SubscribedNewsletterResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.subscribe.dto.response.SubscribedNewsletterResponse(
            s.id,
            n.id,
            coalesce(t.name, n.name),
            n.imageUrl,
            coalesce(t.description, n.description),
            s.unsubscribeUrl,
            s.status,
            n.status,
            n.source
        )
        FROM Subscribe s
        JOIN Newsletter n ON s.newsletterId = n.id
        LEFT JOIN NewsletterTranslation t ON t.newsletterId = n.id
            AND t.language = :language AND :language <> 'ko'
        WHERE s.memberId = :memberId
        ORDER BY n.name
    """)
    List<SubscribedNewsletterResponse> findSubscribedByMemberId(
            @Param("memberId") Long memberId,
            @Param("language") String language
    );

    boolean existsByMemberIdAndNewsletterId(Long memberId, Long newsletterId);

    Optional<Subscribe> findByMemberIdAndNewsletterId(Long memberId, Long newsletterId);

    List<Subscribe> findAllByMemberId(Long memberId);

    int countByMemberId(Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM Subscribe sb
            WHERE sb.memberId = :memberId
    """)
    void bulkDeleteAllByMemberId(Long memberId);

    @Modifying
    @Query("""
            DELETE FROM Subscribe sb
            WHERE sb.memberId = :memberId
              AND sb.newsletterId = :newsletterId
    """)
    void bulkDeleteByMemberIdAndNewsletterId(Long memberId, Long newsletterId);
}
