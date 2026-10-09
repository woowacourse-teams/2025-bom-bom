package news.bombom.notification.domain;

public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED, // 재시도 대상
    DEAD // 재시도 횟수 소진
}
