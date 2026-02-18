package news.bombom.article.repository;

import news.bombom.article.domain.ArticleArrivalNotificationFailed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleArrivalNotificationFailedRepository extends JpaRepository<ArticleArrivalNotificationFailed, Long> {
}
