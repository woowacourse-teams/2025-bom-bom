package news.bombom.notification.domain;

import lombok.Getter;

/**
 * FCM data 영역에서 분기를 위한 비즈니스 알림 타입
 */
@Getter
public enum NotificationPayloadType {
    ARRIVED_ARTICLE("arrivedArticle"),
    TOPIC_NOTIFICATION("topicNotification"),
    CHALLENGE_TODO_REMINDER("challengeTodoReminder"),
    DEFAULT("default"),
    ;

    private final String code;

    NotificationPayloadType(String code) {
        this.code = code;
    }
}
