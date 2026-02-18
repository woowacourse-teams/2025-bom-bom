package news.bombom.notification.domain;

import lombok.Getter;

/**
 * FCM data 영역에서 분기를 위한 비즈니스 알림 타입
 */
@Getter
public enum NotificationPayloadType {

    ARTICLE,
    EVENT,
    DEFAULT
}
