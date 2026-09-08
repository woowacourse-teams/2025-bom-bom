package me.bombom.api.v1.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SendInquiryMessageRequest(
        @NotBlank(message = "문의 내용은 필수 입력 값입니다.") String content,
        @Size(max = 4, message = "이미지는 최대 4장까지 첨부할 수 있습니다.") List<String> imageUrls
) {
}
