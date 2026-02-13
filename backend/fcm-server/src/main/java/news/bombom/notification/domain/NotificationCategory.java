package news.bombom.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationCategory {

    ARTICLE(true),
    EVENT(false),
    ;

    private final boolean defaultSetting;
}
