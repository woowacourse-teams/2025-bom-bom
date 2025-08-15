package me.bombom.api.v1.newsletter.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;
    private final NewsletterDetailRepository newsletterDetailRepository;

    public List<NewsletterResponse> getNewsletters() {
        //임시로 repository 메서드 내부에 Detail 정보 가져오는 것이 불필요
        List<NewsletterResponse> newsletters = newsletterRepository.findNewslettersInfo();
        Collections.shuffle(newsletters); //초기엔 셔플해서 랜덤 순서로 보여주기
        return newsletters;
    }

    public NewsletterWithDetailResponse getNewsletterWithDetail(Long newsletterId) {
        Newsletter newsletter = newsletterRepository.findById(newsletterId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        NewsletterDetail newsletterDetail = newsletterDetailRepository.findById(newsletter.getDetailId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        return NewsletterWithDetailResponse.of(newsletter, newsletterDetail);
    }
}
