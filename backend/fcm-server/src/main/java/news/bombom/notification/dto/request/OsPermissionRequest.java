package news.bombom.notification.dto.request;

import jakarta.validation.constraints.NotNull;

public record OsPermissionRequest(@NotNull Boolean granted) {}
