package me.bombom.api.v1.inquiry.dto.response;

import me.bombom.api.v1.inquiry.domain.InquiryCategory;

public record InquiryCategoryResponse(Long id, String name) {

    public static InquiryCategoryResponse from(InquiryCategory category) {
        return new InquiryCategoryResponse(category.getId(), category.getName());
    }
}
