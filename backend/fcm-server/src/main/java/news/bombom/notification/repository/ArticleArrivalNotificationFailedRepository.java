package news.bombom.notification.repository;

import news.bombom.notification.domain.ArticleArrivalNotificationFailed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleArrivalNotificationFailedRepository extends JpaRepository<ArticleArrivalNotificationFailed, Long> {
}
