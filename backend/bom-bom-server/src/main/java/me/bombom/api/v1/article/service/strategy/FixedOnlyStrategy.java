package me.bombom.api.v1.article.service.strategy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.repository.PreviousArticleRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixedOnlyStrategy implements PreviousArticleStrategy {

    private final PreviousArticleRepository previousArticleRepository;

    @Override
    public List<PreviousArticleResponse> execute(Long newsletterId, int fixedCount, int recentCount) {
        List<PreviousArticleResponse> fixedArticles = previousArticleRepository.findFixedByNewsletterId(newsletterId, fixedCount);
        if (fixedCount != fixedArticles.size()) {
            log.warn("지정된 지난 아티클 개수가 설정과 다릅니다. (뉴스레터 ID: {}, 설정값: {}, 실제 개수: {})", newsletterId, fixedCount, fixedArticles.size());
        }
        return fixedArticles;
    }

    @Override
    public NewsletterPreviousStrategy getStrategy() {
        return NewsletterPreviousStrategy.FIXED_ONLY;
    }
}
