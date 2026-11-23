package me.bombom.api.v1.inquiry.dto.response;

import java.time.LocalDateTime;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;

public record LastMessageResponse(
        String content,
        InquirySenderType senderType,
        String adminNickname,
        LocalDateTime createdAt
) {

    public static LastMessageResponse of(InquiryMessage message, String adminNickname) {
        return new LastMessageResponse(
                message.getContent(),
                message.getSenderType(),
                adminNickname,
                message.getCreatedAt()
        );
    }

    public static LastMessageResponse from(InquiryMessageResponse message) {
        return new LastMessageResponse(
                message.content(),
                message.senderType(),
                message.adminNickname(),
                message.createdAt()
        );
    }
}
