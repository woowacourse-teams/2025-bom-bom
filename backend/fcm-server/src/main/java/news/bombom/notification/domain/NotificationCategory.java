package news.bombom.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationCategory {

    ARTICLE(true, false, null),           // 개별 발송
    EVENT(false, true, "bombom_event"),   // 토픽 발송
    ;

    private final boolean defaultSetting;
    private final boolean useTopic;         // FCM 토픽 사용 여부
    private final String topicName;         // FCM 토픽 이름 (null이면 토픽 사용 안함)

    public boolean getDefaultSetting() {
        return defaultSetting;
    }
}
