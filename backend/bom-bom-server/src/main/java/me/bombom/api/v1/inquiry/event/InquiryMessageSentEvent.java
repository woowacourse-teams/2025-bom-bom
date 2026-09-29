package me.bombom.api.v1.inquiry.event;

public record InquiryMessageSentEvent(String content, Long assigneeId) {
}
