package me.bombom.api.v1.newsletter.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.dto.CategoryResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterListResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterService {

    private static final int SUSPENDED_HIDDEN_AFTER_MONTHS = 6;

    private final NewsletterRepository newsletterRepository;
    private final Clock clock;

    public NewsletterListResponse getNewsletterList(Long memberId, boolean includeSuspended, Long categoryId) {
        LocalDate suspendedHiddenThresholdDate = getSuspendedHiddenThresholdDate();
        List<NewsletterResponse> allNewsletters = newsletterRepository.findNewslettersInfo(
                memberId,
                includeSuspended,
                suspendedHiddenThresholdDate
        );

        List<CategoryResponse> categories = CategoryResponse.from(allNewsletters);

        List<NewsletterResponse> newsletters;
        if (categoryId == null) {
            newsletters = allNewsletters;
        } else {
            newsletters = allNewsletters.stream()
                    .filter(n -> n.categoryId().equals(categoryId))
                    .toList();
        }

        return new NewsletterListResponse(categories, newsletters);
    }

    public NewsletterWithDetailResponse getNewsletterWithDetail(Long newsletterId, Long memberId) {
        return newsletterRepository.findNewsletterWithDetailById(newsletterId, memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletter")
                        .addContext(ErrorContextKeys.NEWSLETTER_ID, newsletterId));
    }

    public Newsletter getNewsletter(Long id) {
        return newsletterRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletter")
                        .addContext(ErrorContextKeys.OPERATION, "getNewsletter")
                        .addContext(ErrorContextKeys.NEWSLETTER_ID, id));
    }

    private LocalDate getSuspendedHiddenThresholdDate() {
        return LocalDate.now(clock).minusMonths(SUSPENDED_HIDDEN_AFTER_MONTHS);
    }
}
