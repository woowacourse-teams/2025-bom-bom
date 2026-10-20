package news.bombom.notification.domain;

public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED, // 재시도 대상
    DEAD, // 재시도 횟수 소진
    SKIPPED // 알림 조건에 충족되지 않아 스킵
}
