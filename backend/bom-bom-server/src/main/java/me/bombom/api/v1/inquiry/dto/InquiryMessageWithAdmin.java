package me.bombom.api.v1.inquiry.dto;

import com.querydsl.core.annotations.QueryProjection;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;

/**
 * 어드민이 작성하지 않았거나 작성 어드민을 찾지 못한 경우 adminNickname은 null이다.
 */
public record InquiryMessageWithAdmin(
        InquiryMessage message,
        String adminNickname
) {

    @QueryProjection
    public InquiryMessageWithAdmin {
    }
}
