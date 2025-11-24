package news.bombomemail.notification.repository;

import news.bombomemail.notification.domain.ArticleArrivalNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleArrivalNotificationRepository extends JpaRepository<ArticleArrivalNotification, Long> {
}
