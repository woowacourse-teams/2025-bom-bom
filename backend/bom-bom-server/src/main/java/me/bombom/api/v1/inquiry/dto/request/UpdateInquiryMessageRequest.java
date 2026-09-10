package me.bombom.api.v1.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateInquiryMessageRequest(
        @NotBlank(message = "문의 내용은 필수 입력 값입니다.")
        @Size(max = 500, message = "문의 내용은 500자를 초과할 수 없습니다.") String content
) {
}
