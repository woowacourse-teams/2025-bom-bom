package me.bombom.api.v1.newsletter.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.service.NewsletterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/newsletters")
public class NewsletterController {

    private final NewsletterService newsletterService;

    @GetMapping
    public List<NewsletterResponse> getNewsletters() {
        return newsletterService.getNewsletters();
    }
}
