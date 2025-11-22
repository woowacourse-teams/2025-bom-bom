package me.bombom.api.v1.article.service.strategy;

import java.util.List;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;

public interface PreviousArticleStrategy {

    List<PreviousArticleResponse> execute(Long newsletterId, int fixedCount, int recentCount);

    NewsletterPreviousStrategy getStrategy();
}
