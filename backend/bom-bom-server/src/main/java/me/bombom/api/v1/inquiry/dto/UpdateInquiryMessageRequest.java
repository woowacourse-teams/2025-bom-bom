package me.bombom.api.v1.inquiry.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateInquiryMessageRequest(
        @NotBlank(message = "문의 내용은 필수 입력 값입니다.") String content
) {
}
