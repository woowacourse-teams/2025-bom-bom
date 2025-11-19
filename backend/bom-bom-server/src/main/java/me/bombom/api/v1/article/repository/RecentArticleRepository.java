package me.bombom.api.v1.article.repository;

import me.bombom.api.v1.article.domain.RecentArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecentArticleRepository extends JpaRepository<RecentArticle, Long> {
}

