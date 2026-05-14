package me.bombom.api.v1.article.service.strategy;

import java.util.List;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import org.springframework.stereotype.Component;

@Component
public class InactiveStrategy implements PreviousArticleStrategy {

    @Override
    public List<PreviousArticleResponse> execute(Long newsletterId, int fixedCount, int recentCount) {
        return List.of();
    }

    @Override
    public NewsletterPreviousStrategy getStrategy() {
        return NewsletterPreviousStrategy.INACTIVE;
    }
}
