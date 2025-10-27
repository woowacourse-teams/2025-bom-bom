package news.bombom.notification.dto.request;

import jakarta.validation.constraints.NotNull;

public record NotificationSettingRequest(@NotNull Boolean enabled) {}
