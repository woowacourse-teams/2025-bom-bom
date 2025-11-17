package me.bombom.api.v1.article.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.dto.request.PreviousArticleRequest;
import me.bombom.api.v1.article.dto.response.PreviousArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.article.service.strategy.PreviousArticleStrategy;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import me.bombom.api.v1.newsletter.repository.NewsletterPreviousPolicyRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PreviousArticleService {

    private static final int PREVIOUS_ARTICLE_KEEP_COUNT = 10;

    @Value("${admin.previous-article.member-id}")
    private Long PREVIOUS_ARTICLE_ADMIN_ID;

    private final ArticleRepository articleRepository;
    private final NewsletterRepository newsletterRepository;
    private final NewsletterPreviousPolicyRepository newsletterPreviousPolicyRepository;
    private final Map<NewsletterPreviousStrategy, PreviousArticleStrategy> strategyMap;

    public PreviousArticleService(
            ArticleRepository articleRepository,
            NewsletterRepository newsletterRepository,
            NewsletterPreviousPolicyRepository newsletterPreviousPolicyRepository,
            List<PreviousArticleStrategy> strategies
    ) {
        this.articleRepository = articleRepository;
        this.newsletterRepository = newsletterRepository;
        this.newsletterPreviousPolicyRepository = newsletterPreviousPolicyRepository;
        this.strategyMap = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(PreviousArticleStrategy::getStrategy, Function.identity()));
    }

    public List<PreviousArticleResponse> getPreviousArticles(PreviousArticleRequest previousArticleRequest) {
        validateNewsletterId(previousArticleRequest.newsletterId());
        NewsletterPreviousPolicy newsletterPreviousPolicy = newsletterPreviousPolicyRepository.findByNewsletterId(previousArticleRequest.newsletterId());
        PreviousArticleStrategy previousArticleStrategy = strategyMap.get(newsletterPreviousPolicy.getStrategy());

        return previousArticleStrategy.execute(
                previousArticleRequest.newsletterId(),
                newsletterPreviousPolicy.getTotalCount(),
                newsletterPreviousPolicy.getFixedCount()
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
    public int cleanupOldPreviousArticles() {
        return articleRepository.cleanupOldPreviousArticles(PREVIOUS_ARTICLE_ADMIN_ID, PREVIOUS_ARTICLE_KEEP_COUNT);
    }

    private void validateNewsletterId(Long newsletterId) {
        if (newsletterId != null && !newsletterRepository.existsById(newsletterId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.OPERATION, "validateNewsletterId")
                    .addContext(ErrorContextKeys.NEWSLETTER_ID, newsletterId);
        }
    }
}
