package news.bombom.challenge.repository;

import java.time.LocalDateTime;
import java.util.List;
import news.bombom.challenge.domain.ChallengeTodoReminderNotification;
import news.bombom.notification.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeTodoReminderNotificationRepository extends JpaRepository<ChallengeTodoReminderNotification, Long> {

    @Query("""
                    SELECT n
                    FROM ChallengeTodoReminderNotification n
                    WHERE n.status IN :statuses
                    AND (n.nextRetryAt IS NULL OR n.nextRetryAt <= :now)
            """)
    List<ChallengeTodoReminderNotification> findRetryCandidates(@Param("statuses") List<NotificationStatus> statuses,
                                                                @Param("now") LocalDateTime now);
}
