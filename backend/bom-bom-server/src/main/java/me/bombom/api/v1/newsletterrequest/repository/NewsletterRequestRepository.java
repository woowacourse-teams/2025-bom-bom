package me.bombom.api.v1.newsletterrequest.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequest;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;
import me.bombom.api.v1.newsletterrequest.dto.NewsletterRequestRow;
import me.bombom.api.v1.newsletterrequest.dto.RegisteredNewsletterUrl;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterSuggestionResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsletterRequestRepository extends JpaRepository<NewsletterRequest, Long> {

    Optional<NewsletterRequest> findByNormalizedUrl(String normalizedUrl);

    @Query("""
            SELECT new me.bombom.api.v1.newsletterrequest.dto.NewsletterRequestRow(
                r.id, r.requestedName, d.name, r.requestedUrl, r.status, r.likeCount,
                r.requesterMemberId, c.name, d.imageUrl, r.newsletterId
            )
            FROM NewsletterRequest r
            LEFT JOIN NewsletterRequestDraft d ON d.newsletterRequestId = r.id
            LEFT JOIN Category c ON c.id = d.categoryId
                        WHERE r.status IN :inProgressStatuses
               OR (r.status = :approved AND r.updatedAt >= :approvedSince)
            ORDER BY r.likeCount DESC, r.id DESC
            """)
    List<NewsletterRequestRow> findBoardRows(
            @Param("inProgressStatuses") List<NewsletterRequestStatus> inProgressStatuses,
            @Param("approved") NewsletterRequestStatus approved,
            @Param("approvedSince") LocalDateTime approvedSince,
            Pageable pageable
    );

    @Query("""
            SELECT new me.bombom.api.v1.newsletterrequest.dto.NewsletterRequestRow(
                r.id, r.requestedName, d.name, r.requestedUrl, r.status, r.likeCount,
                r.requesterMemberId, c.name, d.imageUrl, r.newsletterId
            )
            FROM NewsletterRequest r
            LEFT JOIN NewsletterRequestDraft d ON d.newsletterRequestId = r.id
            LEFT JOIN Category c ON c.id = d.categoryId
                        WHERE r.requesterMemberId = :memberId
               OR EXISTS (
                   SELECT 1 FROM NewsletterRequestLike l
                   WHERE l.newsletterRequestId = r.id AND l.memberId = :memberId
               )
            ORDER BY r.createdAt DESC, r.id DESC
            """)
    List<NewsletterRequestRow> findRowsRequestedOrLikedBy(@Param("memberId") Long memberId);

    @Query("""
            SELECT new me.bombom.api.v1.newsletterrequest.dto.NewsletterRequestRow(
                r.id, r.requestedName, d.name, r.requestedUrl, r.status, r.likeCount,
                r.requesterMemberId, c.name, d.imageUrl, r.newsletterId
            )
            FROM NewsletterRequest r
            LEFT JOIN NewsletterRequestDraft d ON d.newsletterRequestId = r.id
            LEFT JOIN Category c ON c.id = d.categoryId
                        WHERE r.status IN :inProgressStatuses
              AND (REPLACE(r.requestedName, ' ', '') LIKE CONCAT('%', :keyword, '%')
                   OR REPLACE(d.name, ' ', '') LIKE CONCAT('%', :keyword, '%')
                   OR r.normalizedUrl LIKE CONCAT('%', :keyword, '%'))
            ORDER BY r.likeCount DESC, r.id DESC
            """)
    List<NewsletterRequestRow> findRowsByKeyword(
            @Param("inProgressStatuses") List<NewsletterRequestStatus> inProgressStatuses,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT new me.bombom.api.v1.newsletterrequest.dto.response.NewsletterSuggestionResponse(
                n.id, n.name, n.imageUrl
            )
            FROM Newsletter n
            WHERE REPLACE(n.name, ' ', '') LIKE CONCAT('%', :keyword, '%')
            ORDER BY n.id
            """)
    List<NewsletterSuggestionResponse> findNewsletterSuggestions(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT new me.bombom.api.v1.newsletterrequest.dto.RegisteredNewsletterUrl(n.id, d.mainPageUrl, d.subscribeUrl)
            FROM Newsletter n
            JOIN NewsletterDetail d ON d.id = n.detailId
            """)
    List<RegisteredNewsletterUrl> findRegisteredNewsletterUrls();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE NewsletterRequest r
               SET r.likeCount = r.likeCount + :amount
             WHERE r.id = :id
               AND (:amount >= 0 OR r.likeCount > 0)
            """)
    void bulkIncrementLikeCountNotBelowZero(@Param("id") Long id, @Param("amount") int amount);
}
