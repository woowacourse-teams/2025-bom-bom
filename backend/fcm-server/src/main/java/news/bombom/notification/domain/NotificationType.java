package news.bombom.notification.domain;

import lombok.Getter;

/**
 * 지원하는 알림 타입들
 */
@Getter
public enum NotificationType {
    FCM("Firebase Cloud Messaging")
    ;

    private final String description;
    
    NotificationType(String description) {
        this.description = description;
    }
}
