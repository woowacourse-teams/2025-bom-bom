package me.bombom.api.v1.article.repository;

import java.time.LocalDateTime;
import me.bombom.api.v1.article.domain.MarkAsReadEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarkAsReadEventLogRepository extends JpaRepository<MarkAsReadEventLog, MarkAsReadEventLog.Pk> {

    default boolean markIfAbsent(Long memberId, Long articleId) {
        return insertIfAbsent(memberId, articleId) == 1;
    }

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO mark_as_read_event_log (member_id, article_id)
            VALUES (:memberId, :articleId)
            ON DUPLICATE KEY UPDATE member_id = member_id
    """, nativeQuery = true)
    int insertIfAbsent(
            @Param("memberId") Long memberId,
            @Param("articleId") Long articleId
    );

    @Modifying
    @Query("DELETE FROM MarkAsReadEventLog e WHERE e.createdAt < :threshold")
    int deleteOlderThan(@Param("threshold") LocalDateTime threshold);
}
