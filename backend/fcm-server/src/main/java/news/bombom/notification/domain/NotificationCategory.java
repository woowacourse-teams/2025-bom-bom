package news.bombom.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원 알림 설정 및 전송 정책(토픽/개별 발송)을 구분하는 카테고리
 */
@Getter
@RequiredArgsConstructor
public enum NotificationCategory {

    ARTICLE(true, false, null),
    EVENT(false, true, "bombom_event"),
    CHALLENGE_TODO_REMINDER(true, false, null),
    ;

    private final boolean defaultSetting;
    private final boolean useTopic;         // FCM 토픽 사용 여부
    private final String topicName;         // FCM 토픽 이름 (null이면 토픽 사용 안함)

    public boolean getDefaultSetting() {
        return defaultSetting;
    }
}
