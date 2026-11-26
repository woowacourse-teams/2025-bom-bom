package news.bombom.notification.repository;

import news.bombom.notification.domain.NotificationDeviceSendLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDeviceSendLogRepository extends JpaRepository<NotificationDeviceSendLog, Long> {
}
