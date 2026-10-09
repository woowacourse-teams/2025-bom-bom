package me.bombom.api.v1.newsletterrequest.repository;

import java.util.Collection;
import java.util.List;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsletterRequestLikeRepository extends JpaRepository<NewsletterRequestLike, Long> {

    @Modifying
    @Query(value = """
        INSERT IGNORE INTO newsletter_request_like (member_id, newsletter_request_id)
        VALUES (:memberId, :newsletterRequestId)
    """, nativeQuery = true)
    int bulkInsertIgnoreByMemberIdAndNewsletterRequestId(
            @Param("memberId") Long memberId,
            @Param("newsletterRequestId") Long newsletterRequestId
    );

    int deleteByMemberIdAndNewsletterRequestId(Long memberId, Long newsletterRequestId);

    @Query("""
            SELECT l.newsletterRequestId
            FROM NewsletterRequestLike l
            WHERE l.memberId = :memberId AND l.newsletterRequestId IN :newsletterRequestIds
            """)
    List<Long> findLikedNewsletterRequestIds(
            @Param("memberId") Long memberId,
            @Param("newsletterRequestIds") Collection<Long> newsletterRequestIds
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM NewsletterRequestLike l WHERE l.newsletterRequestId = :newsletterRequestId")
    void deleteByNewsletterRequestId(@Param("newsletterRequestId") Long newsletterRequestId);
}
