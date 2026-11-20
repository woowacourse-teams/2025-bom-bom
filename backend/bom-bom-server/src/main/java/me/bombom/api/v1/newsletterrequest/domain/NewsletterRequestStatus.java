package me.bombom.api.v1.newsletterrequest.domain;

import java.util.List;

public enum NewsletterRequestStatus {

    RECEIVED,   // 접수됨
    REVIEWING,  // 초안 준비됨, 운영진 확인 중
    APPROVED,   // 등록됨
    REJECTED;   // 반려됨

    public static final List<NewsletterRequestStatus> IN_PROGRESS = List.of(RECEIVED, REVIEWING);

    public boolean isInProgress() {
        return IN_PROGRESS.contains(this);
    }
}
