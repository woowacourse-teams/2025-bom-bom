package me.bombom.api.v1.newsletter.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.service.NewsletterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/newsletters")
public class NewsletterController {

    private final NewsletterService newsletterService;

    @GetMapping
    public List<NewsletterResponse> getNewsletters() {
        log.info("뉴스레터 시작");
        List<NewsletterResponse> newsletters = newsletterService.getNewsletters();
        log.info("뉴스레터 종료");
        return newsletters;
    }
}
