package me.bombom.api.v1.newsletter.controller;

import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse;
import me.bombom.api.v1.newsletter.service.NewsletterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/newsletters")
public class NewsletterController implements NewsletterControllerApi{

    private final NewsletterService newsletterService;

    @Override
    @GetMapping
    public List<NewsletterResponse> getNewsletters(@LoginMember(nullable = true) Member member) {
        return newsletterService.getNewsletters(member);
    }

    @Override
    @GetMapping("/{id}")
    public NewsletterWithDetailResponse getNewsletterWithDetail(
            @LoginMember(nullable = true) Member member,
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    ) {
        return newsletterService.getNewsletterWithDetail(id, member);
    }
}
