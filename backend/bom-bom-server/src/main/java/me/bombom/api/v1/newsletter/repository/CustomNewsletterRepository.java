package me.bombom.api.v1.newsletter.repository;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;

public interface CustomNewsletterRepository {

    List<NewsletterResponse> findNewslettersInfo(
            Long memberId,
            boolean includeSuspended,
            LocalDate suspendedHiddenThresholdDate
    );

    List<Long> findVisibleCategoryIds(boolean includeSuspended, LocalDate thresholdDate);
}
