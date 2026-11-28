package news.bombom.notification.repository;

import news.bombom.notification.domain.NotificationOpen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationOpenRepository extends JpaRepository<NotificationOpen, Long> {
}
