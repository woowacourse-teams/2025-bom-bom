package me.bombom.api.v1.inquiry.dto.response;

import java.time.LocalDateTime;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;

public record InquiryRoomResponse(
        Long id,
        Long categoryId,
        InquiryStatus status,
        LocalDateTime createdAt,
        LocalDateTime closedAt
) {

    public static InquiryRoomResponse from(InquiryRoom room) {
        return new InquiryRoomResponse(
                room.getId(),
                room.getCategoryId(),
                room.getStatus(),
                room.getCreatedAt(),
                room.getClosedAt()
        );
    }
}
