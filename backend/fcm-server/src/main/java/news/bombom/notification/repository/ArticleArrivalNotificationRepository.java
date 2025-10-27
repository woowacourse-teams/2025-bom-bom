package news.bombom.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import news.bombom.notification.domain.ArticleArrivalNotification;
import news.bombom.notification.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleArrivalNotificationRepository extends JpaRepository<ArticleArrivalNotification, Long> {

    @Query("""
            SELECT n 
            FROM ArticleArrivalNotification n 
            WHERE n.status IN :statuses AND (n.nextRetryAt IS NULL OR n.nextRetryAt <= :now)
    """)
    List<ArticleArrivalNotification> findRetryCandidates(@Param("statuses") List<NotificationStatus> statuses, @Param("now") LocalDateTime now);
}
