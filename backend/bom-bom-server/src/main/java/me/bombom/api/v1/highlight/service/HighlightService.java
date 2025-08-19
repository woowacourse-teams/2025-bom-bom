package me.bombom.api.v1.highlight.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.domain.HighlightLocation;
import me.bombom.api.v1.highlight.dto.request.HighlightCreateRequest;
import me.bombom.api.v1.highlight.dto.request.UpdateHighlightRequest;
import me.bombom.api.v1.highlight.dto.response.ArticleHighlightResponse;
import me.bombom.api.v1.highlight.dto.response.HighlightCountPerNewsletterResponse;
import me.bombom.api.v1.highlight.dto.response.HighlightResponse;
import me.bombom.api.v1.highlight.dto.response.HighlightStatisticsResponse;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HighlightService {

    private final HighlightRepository highlightRepository;
    private final ArticleRepository articleRepository;
    private final NewsletterRepository newsletterRepository;

    public Page<HighlightResponse> getHighlights(Member member, Long articleId, Long newsletterId, Pageable pageable) {
        return highlightRepository.findHighlights(member.getId(), articleId, newsletterId, pageable);
    }

    @Transactional
    public ArticleHighlightResponse create(HighlightCreateRequest request, Member member) {
        Article article = articleRepository.findById(request.articleId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                    .addContext(ErrorContextKeys.ARTICLE_ID, request.articleId()));
        validateArticleOwner(member, article);
        HighlightLocation location = request.location()
                .toHighlightLocation();
        return highlightRepository.findByArticleIdAndHighlightLocation(article.getId(), location)
                .map(ArticleHighlightResponse::from)
                .orElseGet(() -> {
                    Highlight highlight = highlightRepository.save(buildHighlight(request, location));
                    return ArticleHighlightResponse.from(highlight);
                });
    }

    @Transactional
    public void delete(Long id, Member member) {
        findHighlightWithOwnerValidation(id, member);
        highlightRepository.deleteById(id);
    }

    @Transactional
    public ArticleHighlightResponse update(Long id, UpdateHighlightRequest request, Member member) {
        Highlight highlight = findHighlightWithOwnerValidation(id, member);
        updateHighlight(request, highlight);
        return ArticleHighlightResponse.from(highlight);
    }

    public HighlightStatisticsResponse getHighlightNewsletterStatistics(Member member) {
        int total = highlightRepository.countByMemberId(member.getId());
        List<HighlightCountPerNewsletterResponse> newsletters = highlightRepository.countPerNewsletters(member.getId());
        return HighlightStatisticsResponse.of(total, newsletters);
    }

    private void validateArticleOwner(Member member, Article article) {
        if (article.isNotOwner(member.getId())) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                .addContext(ErrorContextKeys.ARTICLE_ID, article.getId())
                .addContext(ErrorContextKeys.ACTUAL_OWNER_ID, article.getMemberId());
        }
    }

    private Highlight buildHighlight(HighlightCreateRequest createRequest, HighlightLocation location) {
        return Highlight.builder()
                .articleId(createRequest.articleId())
                .highlightLocation(location)
                .color(createRequest.color())
                .text(createRequest.text())
                .memo(createRequest.memo())
                .build();
    }

    private Highlight findHighlightWithOwnerValidation(Long id, Member member) {
        Highlight highlight = highlightRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "Highlight")
                    .addContext("highlightId", id));
        Article article = articleRepository.findById(highlight.getArticleId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                    .addContext(ErrorContextKeys.ARTICLE_ID, highlight.getArticleId())
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "Article")
                    .addContext("highlightId", id));
        validateArticleOwner(member, article);
        return highlight;
    }

    private void updateHighlight(UpdateHighlightRequest request, Highlight highlight) {
        if (request.color() != null) {
            highlight.changeColor(request.color());
        }
        if (request.memo() != null) {
            highlight.editMemo(request.memo());
        }
    }
}
