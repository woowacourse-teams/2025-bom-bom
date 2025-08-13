package me.bombom.api.v1.bookmark.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.bookmark.dto.response.BookmarkResponse;
import me.bombom.api.v1.bookmark.dto.response.BookmarkStatusResponse;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ArticleRepository articleRepository;

    public Page<BookmarkResponse> getBookmarks(Long id, Pageable pageable) {
        return bookmarkRepository.findByMemberId(id, pageable);
    }

    public BookmarkStatusResponse getBookmarkStatus(Long memberId, Long articleId) {
        return BookmarkStatusResponse.from(bookmarkRepository.existsByMemberIdAndArticleId(memberId, articleId));
    }

    @Transactional
    public void addBookmark(Long memberId, Long articleId) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext("memberId", memberId)
                .addContext("articleId", articleId));
        validateArticleOwner(memberId, article);
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
    public void deleteBookmark(Long memberId, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", memberId)
                    .addContext("articleId", articleId));
        validateArticleOwner(memberId, article);
        bookmarkRepository.deleteByMemberIdAndArticleId(memberId, articleId);
    }

    private void validateArticleOwner(Long memberId, Article article) {
        if (article.isNotOwner(memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                .addContext("memberId", memberId)
                .addContext("articleId", article.getId())
                .addContext("actualOwnerId", article.getMemberId());
        }
    }
}
