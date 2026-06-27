package me.bombom.api.v1.bookmark.repository;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, CustomBookmarkRepository {

    boolean existsByMemberIdAndArticleId(Long memberId, Long articleId);

    void deleteByMemberIdAndArticleId(Long memberId, Long articleId);

    long countByMemberId(Long memberId);

    @Query("""
            SELECT COUNT(b.id)
            FROM Bookmark b
            WHERE b.memberId = :memberId
              AND b.createdAt >= :start
              AND b.createdAt < :end
            """)
    long countBookmarksInPeriod(
            @Param("memberId") Long memberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Bookmark b WHERE b.memberId = :memberId")
    void bulkDeleteAllByMemberId(Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Bookmark b WHERE b.articleId IN (:articleIds)")
    void bulkDeleteAllByArticleIds(List<Long> articleIds);
}
