package me.bombom.api.v1.newsletter.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;

    public List<NewsletterResponse> getNewsletters(Member member) {
        Long memberId = getMemberId(member);
        return newsletterRepository.findNewslettersInfo(memberId);
    }

    public NewsletterWithDetailResponse getNewsletterWithDetail(Long newsletterId, Member member) {
        Long memberId = getMemberId(member);
        return newsletterRepository.findNewsletterWithDetailById(newsletterId, memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletter")
                        .addContext(ErrorContextKeys.NEWSLETTER_ID, newsletterId));
    }

    private Long getMemberId(Member member) {
        if (member == null) {
            return null;
        }
        return member.getId();
    }
}
