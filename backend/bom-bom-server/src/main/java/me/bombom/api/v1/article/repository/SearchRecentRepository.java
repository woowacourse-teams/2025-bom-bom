package me.bombom.api.v1.article.repository;

import java.time.LocalDateTime;
import me.bombom.api.v1.article.domain.SearchRecent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchRecentRepository extends JpaRepository<SearchRecent, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SearchRecent s WHERE s.arrivedDateTime < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    void deleteByArticleId(Long articleId);
}
