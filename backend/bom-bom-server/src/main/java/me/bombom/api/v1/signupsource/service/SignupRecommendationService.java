package me.bombom.api.v1.signupsource.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.signupsource.domain.SignupRecommendation;
import me.bombom.api.v1.signupsource.dto.response.SignupRecommendationResponse;
import me.bombom.api.v1.signupsource.dto.response.SignupRecommendationsResponse;
import me.bombom.api.v1.signupsource.repository.SignupRecommendationRepository;
import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import me.bombom.api.v1.subscribe.repository.NewsletterSubscriptionCountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignupRecommendationService {

    private final SignupRecommendationRepository signupRecommendationRepository;
    private final NewsletterRepository newsletterRepository;
    private final NewsletterSubscriptionCountRepository newsletterSubscriptionCountRepository;
    private final Clock clock;

    public SignupRecommendationsResponse getRecommendations() {
        List<SignupRecommendation> recommendations = signupRecommendationRepository.findAllByOrderByDisplayOrderAsc();
        if (recommendations.isEmpty()) {
            return new SignupRecommendationsResponse(List.of(), List.of());
        }

        // 휴간 뉴스레터를 빼면(includeSuspended=false) 활성 뉴스레터만 돌아온다.
        Map<Long, NewsletterResponse> activeNewsletters = newsletterRepository.findNewslettersInfo(
                        null,
                        false,
                        LocalDate.now(clock)
                ).stream()
                .collect(Collectors.toMap(NewsletterResponse::newsletterId, Function.identity()));
        // 구독자 수는 뉴스레터 목록 화면과 같은 집계 테이블을 쓴다.
        Map<Long, Long> subscriberCounts = newsletterSubscriptionCountRepository.findAllByNewsletterIdIn(
                        recommendations.stream().map(SignupRecommendation::getNewsletterId).toList()
                ).stream()
                .collect(Collectors.toMap(
                        NewsletterSubscriptionCount::getNewsletterId,
                        subscriptionCount -> (long) subscriptionCount.getTotal()
                ));

        List<SignupRecommendationResponse> newsletters = recommendations.stream()
                .filter(recommendation -> activeNewsletters.containsKey(recommendation.getNewsletterId()))
                .map(recommendation -> SignupRecommendationResponse.of(
                        activeNewsletters.get(recommendation.getNewsletterId()),
                        recommendation,
                        subscriberCounts.getOrDefault(recommendation.getNewsletterId(), 0L)
                ))
                .toList();
        return new SignupRecommendationsResponse(sortCategoriesByCount(newsletters), newsletters);
    }

    private List<String> sortCategoriesByCount(List<SignupRecommendationResponse> newsletters) {
        Map<String, Long> countByCategory = newsletters.stream()
                .collect(Collectors.groupingBy(
                        SignupRecommendationResponse::category,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
        return countByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .toList();
    }
}
