package me.bombom.api.v1.article.repository;

import java.util.List;
import me.bombom.api.v1.article.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticleRepository extends JpaRepository<Article, Long>, CustomArticleRepository {

    List<Article> findAllByMemberId(Long memberId);
    int countAllByMemberId(Long memberId);

    @Query("""
        SELECT count(*) 
        FROM Article AS a 
        INNER JOIN Newsletter AS n 
        ON a.newsletterId = n.id 
        WHERE n.categoryId = :categoryId 
    """)
    int countAllByCategoryId(Long categoryId);
}
