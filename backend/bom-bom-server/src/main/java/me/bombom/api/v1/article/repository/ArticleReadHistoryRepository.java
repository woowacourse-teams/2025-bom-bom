package me.bombom.api.v1.article.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import me.bombom.api.v1.article.domain.ArticleReadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleReadHistoryRepository extends JpaRepository<ArticleReadHistory, Long> {

    Optional<ArticleReadHistory> findByMemberIdAndArticleId(Long memberId, Long articleId);

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO article_read_history (
                member_id,
                article_id,
                newsletter_id,
                category_id,
                read_at,
                created_at,
                updated_at
            )
            VALUES (
                :memberId,
                :articleId,
                :newsletterId,
                :categoryId,
                :readAt,
                NOW(6),
                NOW(6)
            )
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("memberId") Long memberId,
            @Param("articleId") Long articleId,
            @Param("newsletterId") Long newsletterId,
            @Param("categoryId") Long categoryId,
            @Param("readAt") LocalDateTime readAt
    );
}
