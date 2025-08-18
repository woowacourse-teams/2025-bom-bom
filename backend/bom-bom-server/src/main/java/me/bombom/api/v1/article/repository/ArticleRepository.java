package me.bombom.api.v1.article.repository;

import java.util.List;
import me.bombom.api.v1.article.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long>, CustomArticleRepository {
}
