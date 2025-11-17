package me.bombom.api.v1.article.service.strategy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LatestOnlyStrategy implements PreviousArticleStrategy {

    @Value("${admin.previous-article.member-id}")
    private Long PREVIOUS_ARTICLE_ADMIN_ID;

    private final ArticleRepository articleRepository;

    @Override
    public List<PreviousArticleResponse> execute(Long newsletterId, int totalCount, int fixedCount) {
        return articleRepository.findArticlesExceptLatestByMemberIdAndNewsletterId(
                newsletterId,
                PREVIOUS_ARTICLE_ADMIN_ID,
                totalCount
        );
    }

    @Override
    public NewsletterPreviousStrategy getStrategy() {
        return NewsletterPreviousStrategy.LATEST_ONLY;
    }
}
