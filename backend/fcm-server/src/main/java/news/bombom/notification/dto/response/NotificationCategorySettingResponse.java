package news.bombom.notification.dto.response;

import jakarta.validation.constraints.NotNull;
import news.bombom.notification.domain.NotificationCategory;

public record NotificationCategorySettingResponse(

        @NotNull
        NotificationCategory category,

        boolean enabled
) {
}
