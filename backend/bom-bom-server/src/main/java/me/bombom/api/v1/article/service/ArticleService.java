package me.bombom.api.v1.article.service;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.request.ArticlesOptionsRequest;
import me.bombom.api.v1.article.dto.request.PreviousArticleRequest;
import me.bombom.api.v1.article.dto.response.ArticleCountPerNewsletterResponse;
import me.bombom.api.v1.article.dto.response.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.ArticleNewsletterStatisticsResponse;
import me.bombom.api.v1.article.dto.response.ArticleResponse;
import me.bombom.api.v1.article.dto.response.PreviousArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.event.MarkAsReadEvent;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.dto.response.ArticleHighlightResponse;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.pet.ScorePolicyConstants;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private static final int PREVIOUS_ARTICLE_KEEP_COUNT = 10;

    @Value("${admin.previous-article.member-id}")
    private Long PREVIOUS_ARTICLE_ADMIN_ID;

    private final ArticleRepository articleRepository;
    private final TodayReadingRepository todayReadingRepository;
    private final CategoryRepository categoryRepository;
    private final NewsletterRepository newsletterRepository;
    private final HighlightRepository highlightRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MemberRepository memberRepository;

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

    public List<PreviousArticleResponse> getPreviousArticles(PreviousArticleRequest previousArticleRequest) {
        validateNewsletterId(previousArticleRequest.newsletterId());
        return articleRepository.findArticlesByMemberIdAndNewsletterId(
                previousArticleRequest.newsletterId(),
                PREVIOUS_ARTICLE_ADMIN_ID,
                previousArticleRequest.limit()
        );
    }

    public PreviousArticleDetailResponse getPreviousArticleDetail(Long id) {
        return articleRepository.getPreviousArticleDetailsByMemberId(id, PREVIOUS_ARTICLE_ADMIN_ID)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ARTICLE_ID, id)
                        .addContext(ErrorContextKeys.MEMBER_ID, PREVIOUS_ARTICLE_ADMIN_ID)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "article"));
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

    public ArticleNewsletterStatisticsResponse getArticleNewsletterStatistics(Member member, String keyword) {
        String trimmedKeyword = StringUtils.hasText(keyword) ? keyword.strip() : null;
        List<ArticleCountPerNewsletterResponse> countResponse = articleRepository.countPerNewsletter(member.getId(), trimmedKeyword);
        int totalCount = countResponse.stream()
                .mapToInt(ArticleCountPerNewsletterResponse::articleCount)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllByMemberId(Long memberId) {
        articleRepository.deleteAllByMemberId(memberId);
    }

    @Transactional
    public int cleanupOldPreviousArticles() {
        return articleRepository.cleanupOldPreviousArticles(PREVIOUS_ARTICLE_ADMIN_ID, PREVIOUS_ARTICLE_KEEP_COUNT);
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
