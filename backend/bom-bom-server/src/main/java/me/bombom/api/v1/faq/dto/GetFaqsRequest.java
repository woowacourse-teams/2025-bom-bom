package me.bombom.api.v1.faq.dto;

import me.bombom.api.v1.faq.domain.FaqCategory;

public record GetFaqsRequest(
        FaqCategory faqCategory
) {
}
