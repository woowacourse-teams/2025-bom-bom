package me.bombom.api.v1.bookmark.repository;

import me.bombom.api.v1.bookmark.dto.response.BookmarkResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomBookmarkRepository {

    Page<BookmarkResponse> findBookmarks(Long memberId, Long newsletterId, Pageable pageable);

    int countAllByMemberIdAndNewsletterId(Long memberId, Long newsletterId);
}
