package me.bombom.api.v1.inquiry.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryStatus {
    UNCONFIRMED("미확인"),
    IN_PROGRESS("진행중"),
    DONE("완료"),
    ON_HOLD("보류"),
    ;

    private final String description;
}
