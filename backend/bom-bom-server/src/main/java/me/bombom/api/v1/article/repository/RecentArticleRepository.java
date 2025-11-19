package me.bombom.api.v1.article.repository;

import java.time.LocalDateTime;
import me.bombom.api.v1.article.domain.RecentArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecentArticleRepository extends JpaRepository<RecentArticle, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RecentArticle ra WHERE ra.arrivedDateTime < :cutoffDateTime")
    int deleteAllByArrivedDateTimeBefore(@Param("cutoffDateTime") LocalDateTime cutoffDateTime);
}

