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
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.signupsource.domain.SignupRecommendation;
import me.bombom.api.v1.signupsource.dto.response.NewsletterSubscriberCount;
import me.bombom.api.v1.signupsource.dto.response.SignupRecommendationResponse;
import me.bombom.api.v1.signupsource.dto.response.SignupRecommendationsResponse;
import me.bombom.api.v1.signupsource.repository.SignupRecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignupRecommendationService {

    private final SignupRecommendationRepository signupRecommendationRepository;
    private final NewsletterRepository newsletterRepository;
    private final Clock clock;

    public SignupRecommendationsResponse getRecommendations() {
        List<SignupRecommendation> recommendations = signupRecommendationRepository.findAllByOrderByDisplayOrderAsc();
        if (recommendations.isEmpty()) {
            return new SignupRecommendationsResponse(List.of(), List.of());
        }

        Map<Long, NewsletterResponse> activeNewsletters = newsletterRepository.findNewslettersInfo(
                        null,
                        false,
                        LocalDate.now(clock)
                ).stream()
                .filter(newsletter -> newsletter.status() == NewsletterPublicationStatus.ACTIVE)
                .collect(Collectors.toMap(NewsletterResponse::newsletterId, Function.identity()));
        Map<Long, Long> subscriberCounts = signupRecommendationRepository.countSubscribersByNewsletterIds(
                        recommendations.stream().map(SignupRecommendation::getNewsletterId).toList()
                ).stream()
                .collect(Collectors.toMap(
                        NewsletterSubscriberCount::newsletterId,
                        NewsletterSubscriberCount::subscriberCount
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
