package news.bombom.inquiry.repository;

import java.time.LocalDateTime;
import java.util.List;
import news.bombom.inquiry.domain.InquiryMessageArrivalNotification;
import news.bombom.notification.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InquiryMessageArrivalNotificationRepository
        extends JpaRepository<InquiryMessageArrivalNotification, Long> {

    @Query("""
                SELECT n
                FROM InquiryMessageArrivalNotification n
                WHERE n.status IN :statuses
                AND (n.nextRetryAt IS NULL OR n.nextRetryAt <= :now)
            """)
    List<InquiryMessageArrivalNotification> findRetryCandidates(@Param("statuses") List<NotificationStatus> statuses,
                                                                @Param("now") LocalDateTime now);
}
