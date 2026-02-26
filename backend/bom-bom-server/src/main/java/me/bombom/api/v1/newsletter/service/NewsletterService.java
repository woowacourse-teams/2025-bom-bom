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
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterService {

    private static final int SUSPENDED_HIDDEN_AFTER_MONTHS = 6;

    private final NewsletterRepository newsletterRepository;
    private final CategoryRepository categoryRepository;
    private final Clock clock;

    public List<NewsletterResponse> getNewsletters(Long memberId, boolean includeSuspended) {
        LocalDate suspendedHiddenThresholdDate = getSuspendedHiddenThresholdDate();
        return newsletterRepository.findNewslettersInfo(memberId, includeSuspended, suspendedHiddenThresholdDate);
    }

    public List<CategoryResponse> getCategories(boolean includeSuspended) {
        LocalDate suspendedHiddenThresholdDate = getSuspendedHiddenThresholdDate();
        return categoryRepository.findCategories(includeSuspended, suspendedHiddenThresholdDate);
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
