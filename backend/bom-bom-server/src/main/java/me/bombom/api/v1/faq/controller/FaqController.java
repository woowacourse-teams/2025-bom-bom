package me.bombom.api.v1.faq.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.faq.dto.FaqResponse;
import me.bombom.api.v1.faq.dto.GetFaqsOptionsRequest;
import me.bombom.api.v1.faq.service.FaqService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/faqs")
public class FaqController implements FaqControllerApi {

    private final FaqService faqService;

    @Override
    @GetMapping
    public Page<FaqResponse> getFaqs(
            @ModelAttribute GetFaqsOptionsRequest request,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return faqService.getFaqs(request, pageable);
    }
}
