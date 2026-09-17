package me.bombom.api.v1.inquiry.dto;

public record UnresolvedInquiryRoomCounts(long unconfirmedCount, long inProgressCount) {

    public static UnresolvedInquiryRoomCounts of(long unconfirmedCount, long inProgressCount) {
        return new UnresolvedInquiryRoomCounts(unconfirmedCount, inProgressCount);
    }
}
