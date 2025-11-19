package me.bombom.api.v1.article.service.strategy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.repository.PreviousArticleRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LatestOnlyStrategy implements PreviousArticleStrategy {

    private final PreviousArticleRepository previousArticleRepository;

    @Override
    public List<PreviousArticleResponse> execute(Long newsletterId, int fixedCount, int lastestCount) {
        return previousArticleRepository.findExceptLatestByNewsletterId(newsletterId, lastestCount);
    }

    @Override
    public NewsletterPreviousStrategy getStrategy() {
        return NewsletterPreviousStrategy.LATEST_ONLY;
    }
}
