package me.bombom.api.v1.inquiry.dto;

import com.querydsl.core.annotations.QueryProjection;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;

/**
 * 담당자가 배정되지 않았거나 담당자를 찾지 못한 경우 assigneeNickname은 null이다.
 */
public record InquiryRoomWithAssignee(
        InquiryRoom room,
        String assigneeNickname
) {

    @QueryProjection
    public InquiryRoomWithAssignee {
    }
}
