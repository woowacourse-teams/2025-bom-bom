package me.bombom.api.v1.article.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticleCountPerNewsletterResponse;
import me.bombom.api.v1.article.dto.GetArticleNewsletterStatisticsResponse;
import me.bombom.api.v1.article.dto.GetArticlesOptions;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.pet.ScorePolicyConstants;
import me.bombom.api.v1.pet.event.AddArticleScoreEvent;
import me.bombom.api.v1.reading.event.UpdateReadingCountEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final NewsletterRepository newsletterRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public Page<ArticleResponse> getArticles(
            Member member,
            GetArticlesOptions getArticlesOptions,
            Pageable pageable
    ) {
        validateNewsletterName(getArticlesOptions.newsletter());
        return articleRepository.findByMemberId(member.getId(), getArticlesOptions, pageable);
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
        applicationEventPublisher.publishEvent(new UpdateReadingCountEvent(member.getId(), articleId));
        applicationEventPublisher.publishEvent(new AddArticleScoreEvent(member.getId()));
    }

    public GetArticleNewsletterStatisticsResponse getArticleNewsletterStatistics(Member member, String keyword) {
        int totalCount = articleRepository.countAllByMemberId(member.getId(), keyword);
        List<GetArticleCountPerNewsletterResponse> countResponse = newsletterRepository.findAll()
                .stream()
                .map(newsletter -> {
                    int count = articleRepository.countAllByNewsletterIdAndMemberId(
                            member.getId(),
                            newsletter.getId(),
                            keyword
                    );
                    return GetArticleCountPerNewsletterResponse.of(newsletter, count);
                })
                .toList();
        return GetArticleNewsletterStatisticsResponse.of(totalCount, countResponse);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public boolean canAddArticleScore(Long memberId) {
        int todayReadCount = articleRepository.countByMemberIdAndArrivedDateTimeAndIsRead(
                memberId,
                LocalDate.now(),
                true
        );
        return todayReadCount <= ScorePolicyConstants.MAX_TODAY_READING_COUNT;
    }

    public boolean isArrivedToday(Long articleId, Long memberId) {
        Article article = findArticleById(articleId, memberId);
        return article.isArrivedToday();
    }

    private Article findArticleById(Long articleId, Long memberId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "article")
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.ARTICLE_ID, articleId));
    }

    private void validateNewsletterName(String newsletterName) {
        if (newsletterName != null && !newsletterRepository.existsByName(newsletterName)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.OPERATION, "validateNewsletterName")
                    .addContext("newsletterName", newsletterName);
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
