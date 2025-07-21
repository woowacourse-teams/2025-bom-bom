package me.bombom.api.v1.newsletter.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;

    public List<NewsletterResponse> getNewsletters() {
        //임시로 repository 메서드 내부에 Detail 정보 가져오는 것이 불필요
        List<NewsletterResponse> newsletters = newsletterRepository.findNewslettersInfo();
        Collections.shuffle(newsletters); //초기엔 셔플해서 랜덤 순서로 보여주기
        return newsletters;
    }
}
