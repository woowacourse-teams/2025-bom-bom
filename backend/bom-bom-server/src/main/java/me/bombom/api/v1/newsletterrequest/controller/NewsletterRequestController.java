package me.bombom.api.v1.newsletterrequest.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.newsletterrequest.dto.request.CreateNewsletterRequestRequest;
import me.bombom.api.v1.newsletterrequest.dto.response.CheckNewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.CreateNewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestLikeResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestSuggestionResponse;
import me.bombom.api.v1.newsletterrequest.service.NewsletterRequestService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/newsletter-requests")
public class NewsletterRequestController implements NewsletterRequestControllerApi {

    private final NewsletterRequestService newsletterRequestService;

    @Override
    @GetMapping
    public List<NewsletterRequestResponse> getNewsletterRequests(@LoginMember(anonymous = true) Member member) {
        return newsletterRequestService.getNewsletterRequests(member);
    }

    @Override
    @GetMapping("/me")
    public List<NewsletterRequestResponse> getMyNewsletterRequests(@LoginMember Member member) {
        return newsletterRequestService.getMyNewsletterRequests(member);
    }

    @Override
    @GetMapping("/suggestions")
    public NewsletterRequestSuggestionResponse getNewsletterRequestSuggestions(
            @LoginMember(anonymous = true) Member member,
            @RequestParam(required = false) String keyword
    ) {
        return newsletterRequestService.getNewsletterRequestSuggestions(member, keyword);
    }

    @Override
    @GetMapping("/check")
    public CheckNewsletterRequestResponse checkNewsletterRequest(@RequestParam String url) {
        return newsletterRequestService.checkNewsletterRequest(url);
    }

    @Override
    @PostMapping
    public CreateNewsletterRequestResponse createNewsletterRequest(
            @LoginMember Member member,
            @Valid @RequestBody CreateNewsletterRequestRequest request
    ) {
        return newsletterRequestService.createNewsletterRequest(member, request);
    }

    @Override
    @PutMapping("/{id}/like")
    public NewsletterRequestLikeResponse addNewsletterRequestLike(
            @LoginMember Member member,
            @PathVariable Long id
    ) {
        return newsletterRequestService.addNewsletterRequestLike(member, id);
    }

    @Override
    @DeleteMapping("/{id}/like")
    public NewsletterRequestLikeResponse deleteNewsletterRequestLike(
            @LoginMember Member member,
            @PathVariable Long id
    ) {
        return newsletterRequestService.deleteNewsletterRequestLike(member, id);
    }
}
