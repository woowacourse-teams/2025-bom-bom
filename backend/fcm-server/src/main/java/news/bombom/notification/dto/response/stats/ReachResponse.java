package news.bombom.notification.dto.response.stats;

public record ReachResponse(
        long membersWithToken,
        long devicesWithToken,
        long devicesOsPermissionGranted,
        long devicesOsPermissionDenied,
        long devicesOsPermissionUnknown
) {
}
