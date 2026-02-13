package news.bombom.notification.dto.response;

import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import news.bombom.notification.domain.MemberNotificationSetting;
import news.bombom.notification.domain.NotificationCategory;

public record NotificationCategorySettingResponse(

        @NotNull NotificationCategory category,
        boolean enabled
) {

    public static NotificationCategorySettingResponse from(MemberNotificationSetting setting, NotificationCategory category) {
        return new NotificationCategorySettingResponse(category, setting.isEnabled(category));
    }

    public static List<NotificationCategorySettingResponse> from(MemberNotificationSetting setting) {
        return Arrays.stream(NotificationCategory.values())
                .map(category -> from(setting, category))
                .toList();
    }
}
