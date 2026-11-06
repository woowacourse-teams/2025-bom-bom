package me.bombom.api.v1.inquiry.dto.response;

import java.time.LocalDateTime;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;

public record InquiryRoomResponse(
        Long id,
        Long categoryId,
        InquiryStatus status,
        Long assigneeId,
        String assigneeNickname,
        boolean hasUnreadMessage,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        LastMessageResponse lastMessage
) {

    public static InquiryRoomResponse ofUnassignedRoom(InquiryRoom room, boolean hasUnreadMessage) {
        return of(room, hasUnreadMessage, null);
    }

    public static InquiryRoomResponse of(
            InquiryRoom room,
            boolean hasUnreadMessage,
            String assigneeNickname
    ) {
        return of(room, hasUnreadMessage, assigneeNickname, null);
    }

    public static InquiryRoomResponse of(
            InquiryRoom room,
            boolean hasUnreadMessage,
            String assigneeNickname,
            LastMessageResponse lastMessage
    ) {
        return new InquiryRoomResponse(
                room.getId(),
                room.getCategoryId(),
                room.getStatus(),
                room.getAssigneeId(),
                assigneeNickname,
                hasUnreadMessage,
                room.getCreatedAt(),
                room.getClosedAt(),
                lastMessage
        );
    }
}
