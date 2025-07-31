package me.bombom.api.v1.highlight.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.domain.HighlightLocation;
import me.bombom.api.v1.highlight.dto.request.HighlightCreateRequest;
import me.bombom.api.v1.highlight.dto.response.HighlightResponse;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HighlightService {

    private final HighlightRepository highlightRepository;
    private final ArticleRepository articleRepository;

    public List<HighlightResponse> getHighlights(Long articleId, Member member) {
        validateArticleExist(articleId);
        return HighlightResponse.from(highlightRepository.findByArticleId(articleId));
    }

    @Transactional
    public void create(HighlightCreateRequest createRequest, Member member) {
        validateArticleExist(createRequest.articleId());
        HighlightLocation highlightLocation = new HighlightLocation(
                createRequest.startOffset(),
                createRequest.startXPath(),
                createRequest.endOffset(),
                createRequest.endXPath()
        );
        if (highlightRepository.existsByArticleIdAndHighlightLocation(createRequest.articleId(), highlightLocation)) {
            return;
        }
        Article article = articleRepository.findById(createRequest.articleId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        validateArticleOwner(member, article);
        Highlight highlight = Highlight.builder()
                .articleId(createRequest.articleId())
                .highlightLocation(highlightLocation)
                .color(createRequest.color())
                .text(createRequest.text())
                .build();
        highlightRepository.save(highlight);
    }

    @Transactional
    public void delete(Long id, Member member) {
        Highlight highlight = highlightRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        Article article = articleRepository.findById(highlight.getArticleId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        validateArticleOwner(member, article);
        highlightRepository.deleteById(id);
    }

    @Transactional
    public void changeColor(Long id, String color, Member member) {
        Highlight highlight = highlightRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        Article article = articleRepository.findById(highlight.getArticleId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        validateArticleOwner(member, article);
        highlight.changeColor(color);
    }

    private void validateArticleExist(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND);
        }
    }

    private static void validateArticleOwner(Member member, Article article) {
        if (article.isNotOwner(member.getId())) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE);
        }
    }
}
