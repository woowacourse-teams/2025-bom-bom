package me.bombom.api.v1.inquiry.dto;

import jakarta.validation.constraints.NotNull;

public record CreateInquiryRoomRequest(
        @NotNull(message = "카테고리는 필수 입력 값입니다.") Long categoryId
) {
}
