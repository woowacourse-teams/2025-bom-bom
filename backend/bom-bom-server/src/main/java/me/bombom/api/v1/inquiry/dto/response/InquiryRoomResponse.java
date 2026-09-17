package me.bombom.api.v1.inquiry.dto.response;

import java.time.LocalDateTime;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;

public record InquiryRoomResponse(
        Long id,
        Long categoryId,
        InquiryStatus status,
        boolean hasUnreadMessage,
        LocalDateTime createdAt,
        LocalDateTime closedAt
) {

    public static InquiryRoomResponse of(InquiryRoom room, boolean hasUnreadMessage) {
        return new InquiryRoomResponse(
                room.getId(),
                room.getCategoryId(),
                room.getStatus(),
                hasUnreadMessage,
                room.getCreatedAt(),
                room.getClosedAt()
        );
    }
}
