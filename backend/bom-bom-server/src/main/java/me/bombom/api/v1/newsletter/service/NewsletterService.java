package me.bombom.api.v1.newsletter.service;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;

    public List<NewsletterResponse> getNewsletters() {
        List<Newsletter> newsletters = newsletterRepository.findAllByOrderBySubscribeCountDescNameAsc();

        Collections.shuffle(newsletters); //초기엔 셔플해서 랜덤 순서로 보여주기

        return NewsletterResponse.from(newsletters);
    }
}
