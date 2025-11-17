package me.bombom.api.v1.article.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.dto.request.PreviousArticleRequest;
import me.bombom.api.v1.article.dto.response.PreviousArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreviousArticleService {

    private static final int PREVIOUS_ARTICLE_KEEP_COUNT = 10;

    @Value("${admin.previous-article.member-id}")
    private Long PREVIOUS_ARTICLE_ADMIN_ID;

    private final ArticleRepository articleRepository;
    private final NewsletterRepository newsletterRepository;

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
