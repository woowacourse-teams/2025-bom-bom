package me.bombom.api.v1.newsletter.repository;

import java.util.List;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;

public interface CustomNewsletterRepository {

    List<NewsletterResponse> findNewslettersInfo(Long memberId, boolean includeSuspended);
}
