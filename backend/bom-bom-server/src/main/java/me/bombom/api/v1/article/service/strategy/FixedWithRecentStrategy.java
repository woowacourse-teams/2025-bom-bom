package me.bombom.api.v1.article.service.strategy;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.repository.PreviousArticleRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixedWithRecentStrategy implements PreviousArticleStrategy {

    private final PreviousArticleRepository previousArticleRepository;

    @Override
    public List<PreviousArticleResponse> execute(Long newsletterId, int fixedCount, int recentCount) {
        List<PreviousArticleResponse> fixedArticles = findFixedArticles(newsletterId, fixedCount);
        List<PreviousArticleResponse> recentArticles = findRecentArticles(newsletterId, recentCount);
        return Stream.concat(fixedArticles.stream(), recentArticles.stream())
                .distinct()
                .toList();
    }

    @Override
    public NewsletterPreviousStrategy getStrategy() {
        return NewsletterPreviousStrategy.FIXED_WITH_RECENT;
    }

    private List<PreviousArticleResponse> findFixedArticles(Long newsletterId, int fixedCount) {
        if (fixedCount <= 0) {
            return List.of();
        }
        List<PreviousArticleResponse> articles = previousArticleRepository.findFixedByNewsletterId(newsletterId, fixedCount);
        if (fixedCount != articles.size()) {
            log.warn("지정된 지난 아티클 개수가 설정과 다릅니다. (뉴스레터 ID: {}, 설정값: {}, 실제 개수: {})",
                    newsletterId, fixedCount, articles.size());
        }
        return articles;
    }

    private List<PreviousArticleResponse> findRecentArticles(Long newsletterId, int recentCount) {
        if (recentCount <= 0) {
            return List.of();
        }
        return previousArticleRepository.findExceptLatestByNewsletterId(newsletterId, recentCount);
    }
}
