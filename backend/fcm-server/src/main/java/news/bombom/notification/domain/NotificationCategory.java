package news.bombom.notification.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationCategory {

    ARTICLE(true),
    EVENT(false),
    ;

    private final boolean defaultSetting;

    public boolean getDefaultSetting() {
        return defaultSetting;
    }
}
