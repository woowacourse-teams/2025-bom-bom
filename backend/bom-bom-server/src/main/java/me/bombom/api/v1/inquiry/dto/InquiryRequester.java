package me.bombom.api.v1.inquiry.dto;

import me.bombom.api.v1.member.domain.Member;

public record InquiryRequester(Long memberId, String guestId) {

    public static InquiryRequester of(Member member, String guestId) {
        return new InquiryRequester(member == null ? null : member.getId(), guestId);
    }

    public boolean isMember() {
        return memberId != null;
    }
}
