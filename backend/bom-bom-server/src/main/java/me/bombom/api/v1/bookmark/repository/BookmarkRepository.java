package me.bombom.api.v1.bookmark.repository;

import me.bombom.api.v1.bookmark.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, CustomBookmarkRepository {

    boolean existsByMemberIdAndArticleId(Long memberId, Long articleId);

    void deleteByArticleIdAndMemberId(Long articleId, Long memberId);
}
