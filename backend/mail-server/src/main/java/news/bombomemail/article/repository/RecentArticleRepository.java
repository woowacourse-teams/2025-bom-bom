package news.bombomemail.article.repository;

import news.bombomemail.article.domain.RecentArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecentArticleRepository extends JpaRepository<RecentArticle, Long> {
}
