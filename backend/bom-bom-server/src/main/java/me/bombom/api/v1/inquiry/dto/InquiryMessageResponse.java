package me.bombom.api.v1.inquiry.dto;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.domain.InquiryMessageImage;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;

public record InquiryMessageResponse(
        Long id,
        Long roomId,
        InquirySenderType senderType,
        Long adminId,
        String content,
        List<String> imageUrls,
        LocalDateTime createdAt
) {

    public static InquiryMessageResponse of(InquiryMessage message, List<InquiryMessageImage> images) {
        List<String> imageUrls = images.stream()
                .map(InquiryMessageImage::getImageUrl)
                .toList();
        return new InquiryMessageResponse(
                message.getId(),
                message.getRoomId(),
                message.getSenderType(),
                message.getAdminId(),
                message.getContent(),
                imageUrls,
                message.getCreatedAt()
        );
    }
}
