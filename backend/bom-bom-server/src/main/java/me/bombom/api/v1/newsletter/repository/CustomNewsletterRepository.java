package me.bombom.api.v1.newsletter.repository;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.domain.NewsletterLanguage;

public interface CustomNewsletterRepository {

    List<NewsletterResponse> findNewslettersInfo(
            Long memberId,
            boolean includeSuspended,
            LocalDate suspendedHiddenThresholdDate,
            NewsletterLanguage language
    );
}
