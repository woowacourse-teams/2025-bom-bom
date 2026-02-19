package me.bombom.api.v1.challenge.repository;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.challenge.domain.notification.ChallengeTodoReminderNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeTodoReminderNotificationRepository extends JpaRepository<ChallengeTodoReminderNotification, Long> {

    @Query("""
                SELECT n.memberId
                FROM ChallengeTodoReminderNotification n
                WHERE n.challengeId = :challengeId
                  AND n.createdAt >= :startAt
                  AND n.createdAt <= :endAt
            """)
    List<Long> findMemberIdsByChallengeIdAndCreatedAtBetween(
            @Param("challengeId") Long challengeId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
