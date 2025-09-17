package me.bombom.api.v1.article.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.request.ArticlesOptionsRequest;
import me.bombom.api.v1.article.dto.response.ArticleCountPerNewsletterResponse;
import me.bombom.api.v1.article.dto.response.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.ArticleNewsletterStatisticsResponse;
import me.bombom.api.v1.article.dto.response.ArticleResponse;
import me.bombom.api.v1.article.event.MarkAsReadEvent;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.dto.response.ArticleHighlightResponse;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.pet.ScorePolicyConstants;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TodayReadingRepository todayReadingRepository;
    private final CategoryRepository categoryRepository;
    private final NewsletterRepository newsletterRepository;
    private final HighlightRepository highlightRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public Page<ArticleResponse> getArticles(
            Member member,
            ArticlesOptionsRequest articlesOptionsRequest,
            Pageable pageable
    ) {
        validateNewsletterId(articlesOptionsRequest.newsletterId());
        return articleRepository.findArticles(member.getId(), articlesOptionsRequest, pageable);
    }

    public ArticleDetailResponse getArticleDetail(Long id, Member member) {
        Article article = findArticleById(id, member.getId());
        validateArticleOwner(article, member.getId());
        Newsletter newsletter = newsletterRepository.findById(article.getNewsletterId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletter")
                        .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                        .addContext(ErrorContextKeys.NEWSLETTER_ID, article.getNewsletterId()));
        Category category = categoryRepository.findById(newsletter.getCategoryId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "category")
                        .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                        .addContext("categoryId", newsletter.getCategoryId()));
        return ArticleDetailResponse.of(article, newsletter, category);
    }

    @Transactional
    public void markAsRead(Long articleId, Member member) {
        Article article = findArticleById(articleId, member.getId());
        if (article.isRead()) {
            return;
        }
        validateArticleOwner(article, member.getId());
        article.markAsRead();

        applicationEventPublisher.publishEvent(new MarkAsReadEvent(member.getId(), articleId));
        log.info("Published event: MarkAsReadEvent - memberId={}, articleId={}",
                member.getId(), articleId);
    }

    // FIXME: 테스트를 위해 임시 주석
//    public ArticleNewsletterStatisticsResponse getArticleNewsletterStatistics(Member member, String keyword) {
//        String trimmedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.strip();
//
//        List<ArticleCountPerNewsletterResponse> rows = articleRepository.countPerNewsletter(member.getId(), trimmedKeyword);
//        int total = rows.stream()
//                .mapToInt(ArticleCountPerNewsletterResponse::articleCount)
//                .sum();
//        return ArticleNewsletterStatisticsResponse.of(total, rows);
//    }

    public ArticleNewsletterStatisticsResponse getArticleNewsletterStatistics(Member member, String keyword) {
        List<ArticleCountPerNewsletterResponse> countResponse = newsletterRepository.findAll()
                .stream()
                .map(newsletter -> {
                    int count = articleRepository.countAllByNewsletterIdAndMemberId(
                            member.getId(),
                            newsletter.getId(),
                            keyword
                    );
                    return ArticleCountPerNewsletterResponse.of(newsletter, count);
                })
                .filter(response -> response.articleCount() > 0)
                .sorted(
                        Comparator.comparingLong(ArticleCountPerNewsletterResponse::articleCount)
                                .reversed()
                )
                .toList();

        int totalCount = countResponse.stream()
                .mapToInt(response -> (int) response.articleCount())
                .sum();

        return ArticleNewsletterStatisticsResponse.of(totalCount, countResponse);
    }

    public boolean canAddArticleScore(Long memberId) {
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "TodayReading"));

        return todayReading.getCurrentCount() <= ScorePolicyConstants.MAX_TODAY_READING_COUNT;
    }

    public boolean isArrivedToday(Long articleId, Long memberId) {
        Article article = findArticleById(articleId, memberId);
        return article.isArrivedToday();
    }

    public List<ArticleHighlightResponse> getHighlights(Member member, Long articleId) {
        Article article = findArticleById(articleId, member.getId());
        validateArticleOwner(article, member.getId());

        return highlightRepository.findAllByArticleId(articleId)
                .stream()
                .sorted(Comparator.comparing(Highlight::getCreatedAt).reversed())
                .map(highlight -> ArticleHighlightResponse.from(highlight))
                .toList();
    }

    private Article findArticleById(Long articleId, Long memberId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "article")
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.ARTICLE_ID, articleId));
    }

    private void validateNewsletterId(Long newsletterId) {
        if (newsletterId != null && !newsletterRepository.existsById(newsletterId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.OPERATION, "validateNewsletterId")
                    .addContext(ErrorContextKeys.NEWSLETTER_ID, newsletterId);
        }
    }

    private void validateArticleOwner(Article article, Long memberId) {
        if (article.isNotOwner(memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext(ErrorContextKeys.ARTICLE_ID, article.getId())
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.OPERATION, "validateArticleOwner")
                    .addContext(ErrorContextKeys.ACTUAL_OWNER_ID, article.getMemberId());
        }
    }
}
