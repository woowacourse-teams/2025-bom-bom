package news.bombom.notification.dto.response;

import jakarta.validation.constraints.NotNull;

public record NotificationResultResponse(
        int successCount,
        int failCount,
        int skippedCount,

        @NotNull
        String errorMessages
) {
    public int totalDevices() {
        return successCount + failCount + skippedCount;
    }
}
