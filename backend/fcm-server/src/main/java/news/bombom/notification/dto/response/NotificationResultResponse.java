package news.bombom.fcm.dto.response;

public record NotificationResultResponse(
        int successCount,
        int failCount,
        int skippedCount,
        String errorMessages
) {
    public int totalDevices() {
        return successCount + failCount + skippedCount;
    }
}
