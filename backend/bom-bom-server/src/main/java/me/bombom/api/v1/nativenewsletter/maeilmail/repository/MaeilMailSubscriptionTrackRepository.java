package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.domain.NewsletterSource;
import me.bombom.api.v1.subscribe.domain.SubscribeStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MaeilMailSubscriptionTrackRepository extends JpaRepository<MaeilMailSubscriptionTrack, Long> {

    @Query("""
            SELECT t FROM MaeilMailSubscriptionTrack t
            JOIN Subscribe s ON s.id = t.subscribeId
            JOIN Newsletter n ON n.id = s.newsletterId
            WHERE t.id > :lastTrackId
            AND s.status = :subscribeStatus
            AND n.source = :source
            AND n.status = :newsletterStatus
            AND (t.lastIssuedDate IS NULL OR t.lastIssuedDate <> :issueDate)
            ORDER BY t.id
            """)
    List<MaeilMailSubscriptionTrack> findIssueTargetsAfterId(
            @Param("issueDate") LocalDate issueDate,
            @Param("subscribeStatus") SubscribeStatus subscribeStatus,
            @Param("source") NewsletterSource source,
            @Param("newsletterStatus") NewsletterPublicationStatus newsletterStatus,
            @Param("lastTrackId") Long lastTrackId,
            Pageable pageable
    );

    @Transactional
    @Modifying
    @Query("""
            UPDATE MaeilMailSubscriptionTrack t
            SET t.curriculumIndex = t.curriculumIndex + 1,
                t.lastIssuedDate = :issueDate
            WHERE t.id IN :ids
            """)
    void markIssuedByIds(
            @Param("ids") List<Long> ids,
            @Param("issueDate") LocalDate issueDate
    );
}
