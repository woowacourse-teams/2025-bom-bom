package me.bombom.api.v1.bookmark.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.bookmark.dto.BookmarkResponse;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
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

    @Transactional
    public void save(Long memberId, Long articleId) {
        if (bookmarkRepository.existsByMemberIdAndArticleId(memberId, articleId)) {
            return;
        }
        Bookmark bookmark = Bookmark.builder()
                .memberId(memberId)
                .articleId(articleId)
                .build();
        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void deleteByArticleId(Long memberId, Long articleId) {
        bookmarkRepository.deleteByArticleIdAndMemberId(memberId, articleId);
    }
}
