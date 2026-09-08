package me.bombom.api.v1.inquiry.dto;

public record InquiryRequester(Long memberId, String guestId) {

    public boolean isMember() {
        return memberId != null;
    }
}
