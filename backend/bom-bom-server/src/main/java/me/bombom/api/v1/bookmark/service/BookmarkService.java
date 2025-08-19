package me.bombom.api.v1.bookmark.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.bookmark.domain.Bookmark;
import me.bombom.api.v1.bookmark.dto.response.BookmarkResponse;
import me.bombom.api.v1.bookmark.dto.response.BookmarkStatusResponse;
import me.bombom.api.v1.bookmark.dto.response.GetBookmarkCountPerNewsletterResponse;
import me.bombom.api.v1.bookmark.dto.response.GetBookmarkNewsletterStatisticsResponse;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ArticleRepository articleRepository;
    private final NewsletterRepository newsletterRepository;

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
                .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                .addContext(ErrorContextKeys.ARTICLE_ID, articleId));
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
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ARTICLE_ID, articleId));
        validateArticleOwner(memberId, article);
        bookmarkRepository.deleteByMemberIdAndArticleId(memberId, articleId);
    }

    private void validateArticleOwner(Long memberId, Article article) {
        if (article.isNotOwner(memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                .addContext(ErrorContextKeys.ARTICLE_ID, article.getId())
                .addContext(ErrorContextKeys.ACTUAL_OWNER_ID, article.getMemberId());
        }
    }

    public GetBookmarkNewsletterStatisticsResponse getBookmarkNewsletterStatistics(Member member) {
        List<GetBookmarkCountPerNewsletterResponse> countResponse = newsletterRepository.findAll()
                .stream()
                .map(newsletter -> {
                    int count = bookmarkRepository.countAllByMemberIdAndNewsletterId(member.getId(),
                            newsletter.getId());
                    return GetBookmarkCountPerNewsletterResponse.of(newsletter, count);
                })
                .filter(response -> response.bookmarkCount() > 0)
                .toList();

        int totalCount = countResponse.stream()
                .mapToInt(response -> (int) response.bookmarkCount())
                .sum();

        return GetBookmarkNewsletterStatisticsResponse.of(totalCount, countResponse);
    }
}
