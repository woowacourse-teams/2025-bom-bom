package me.bombom.api.v1.article.repository;

import me.bombom.api.v1.article.domain.ArticleArrivalNotificationFailed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleArrivalNotificationFailedRepository extends JpaRepository<ArticleArrivalNotificationFailed, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM ArticleArrivalNotificationFailed f
            WHERE f.memberId = :memberId
            """)
    void bulkDeleteAllByMemberId(@Param("memberId") Long memberId);
}
