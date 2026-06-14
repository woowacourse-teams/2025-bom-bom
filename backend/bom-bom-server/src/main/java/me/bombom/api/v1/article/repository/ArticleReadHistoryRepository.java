package me.bombom.api.v1.article.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.article.domain.ArticleReadHistory;
import me.bombom.api.v1.reading.dto.DailyReadCount;
import me.bombom.api.v1.reading.dto.FrequentReadNewsletter;
import me.bombom.api.v1.reading.dto.ReadCountComparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleReadHistoryRepository extends JpaRepository<ArticleReadHistory, Long> {

    Optional<ArticleReadHistory> findByMemberIdAndArticleId(Long memberId, Long articleId);

    @Query("""
            SELECT new me.bombom.api.v1.reading.dto.ReadCountComparison(
                COUNT(CASE WHEN h.readAt >= :currentStart AND h.readAt < :currentEnd THEN h.id ELSE NULL END),
                COUNT(CASE WHEN h.readAt >= :previousStart AND h.readAt < :currentStart THEN h.id ELSE NULL END)
            )
            FROM ArticleReadHistory h
            WHERE h.memberId = :memberId
              AND h.readAt >= :previousStart
              AND h.readAt < :currentEnd
            """)
    ReadCountComparison countReadsInPeriods(
            @Param("memberId") Long memberId,
            @Param("previousStart") LocalDateTime previousStart,
            @Param("currentStart") LocalDateTime currentStart,
            @Param("currentEnd") LocalDateTime currentEnd
    );

    @Query("""
            SELECT new me.bombom.api.v1.reading.dto.DailyReadCount(
                day(h.readAt),
                COUNT(h.id)
            )
            FROM ArticleReadHistory h
            WHERE h.memberId = :memberId
              AND h.readAt >= :start
              AND h.readAt < :end
            GROUP BY day(h.readAt)
            """)
    List<DailyReadCount> countDailyReads(
            @Param("memberId") Long memberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT new me.bombom.api.v1.reading.dto.FrequentReadNewsletter(
                n.id,
                n.name,
                COUNT(h.id)
            )
            FROM ArticleReadHistory h
            JOIN Newsletter n ON n.id = h.newsletterId
            WHERE h.memberId = :memberId
              AND h.readAt >= :start
              AND h.readAt < :end
            GROUP BY n.id, n.name
            ORDER BY COUNT(h.id) DESC, n.id ASC
            LIMIT :limit
            """)
    List<FrequentReadNewsletter> findFrequentReadNewsletters(
            @Param("memberId") Long memberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("limit") int limit
    );

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
