package news.bombom.notification.dto.response;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import news.bombom.notification.domain.MemberNotificationSetting;
import news.bombom.notification.domain.NotificationCategory;

public record NotificationCategorySettingResponse(

        @NotNull NotificationCategory category,
        boolean enabled
) {

    public static List<NotificationCategorySettingResponse> from(List<MemberNotificationSetting> settings) {
        return settings.stream()
                .map(NotificationCategorySettingResponse::from)
                .toList();
    }

    public static NotificationCategorySettingResponse from(MemberNotificationSetting setting) {
        return new NotificationCategorySettingResponse(setting.getCategory(), setting.isEnabled());
    }
}
