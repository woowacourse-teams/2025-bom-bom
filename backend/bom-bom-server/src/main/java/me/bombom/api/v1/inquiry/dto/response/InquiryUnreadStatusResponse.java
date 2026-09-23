package me.bombom.api.v1.inquiry.dto.response;

public record InquiryUnreadStatusResponse(boolean hasUnread) {

    public static InquiryUnreadStatusResponse of(boolean hasUnread) {
        return new InquiryUnreadStatusResponse(hasUnread);
    }
}
