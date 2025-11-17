package me.bombom.api.v1.article.service.strategy;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.article.repository.PreviousArticleRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixedWithLatestStrategy implements PreviousArticleStrategy {

    @Value("${admin.previous-article.member-id}")
    private Long PREVIOUS_ARTICLE_ADMIN_ID;

    private final PreviousArticleRepository previousArticleRepository;
    private final ArticleRepository articleRepository;

    @Override
    public List<PreviousArticleResponse> execute(Long newsletterId, int totalCount, int fixedCount) {
        int latestCount = totalCount - fixedCount;
        List<PreviousArticleResponse> fixedArticles = previousArticleRepository.findByNewsletterId(newsletterId, fixedCount);
        if (fixedCount != fixedArticles.size()) {
            log.warn("지정된 지난 아티클 개수가 설정과 다릅니다. (뉴스레터 ID: {}, 설정값: {}, 실제 개수: {})",
                    newsletterId, fixedCount, fixedArticles.size());
        }

        List<PreviousArticleResponse> latestArticles = articleRepository.findArticlesExceptLatestByMemberIdAndNewsletterId(
                newsletterId,
                PREVIOUS_ARTICLE_ADMIN_ID,
                latestCount
        );

        //TODO: 순서가 보장되나 ?
        return Stream.concat(fixedArticles.stream(), latestArticles.stream())
                .distinct()
                .toList();
    }

    @Override
    public NewsletterPreviousStrategy getStrategy() {
        return NewsletterPreviousStrategy.FIXED_WITH_LATEST;
    }
}
