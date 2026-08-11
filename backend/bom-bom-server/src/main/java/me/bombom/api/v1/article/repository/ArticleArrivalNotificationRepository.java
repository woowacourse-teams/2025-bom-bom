package me.bombom.api.v1.article.repository;

import me.bombom.api.v1.article.domain.ArticleArrivalNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleArrivalNotificationRepository extends JpaRepository<ArticleArrivalNotification, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM ArticleArrivalNotification n
            WHERE n.memberId = :memberId
            """)
    void bulkDeleteAllByMemberId(@Param("memberId") Long memberId);
}
