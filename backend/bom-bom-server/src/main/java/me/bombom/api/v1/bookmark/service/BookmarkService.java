package me.bombom.api.v1.bookmark.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.bookmark.dto.BookmarkResponse;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    public Page<BookmarkResponse> getBookmarks(Long id, Pageable pageable) {
        return bookmarkRepository.findByMemberId(id, pageable);
    }

    public boolean getBookmarkStatus(Long memberId, Long articleId) {
        return bookmarkRepository.existsByMemberIdAndArticleId(memberId, articleId);
    }
}
