package me.bombom.api.v1.bookmark.repository;

import java.util.List;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, CustomBookmarkRepository {

    boolean existsByMemberIdAndArticleId(Long memberId, Long articleId);

    void deleteByMemberIdAndArticleId(Long memberId, Long articleId);

    long countByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Bookmark b WHERE b.memberId = :memberId")
    void deleteAllByMemberId(Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Bookmark b WHERE b.articleId IN (:articleIds)")
    void deleteAllByArticleIds(List<Long> articleIds);
}
