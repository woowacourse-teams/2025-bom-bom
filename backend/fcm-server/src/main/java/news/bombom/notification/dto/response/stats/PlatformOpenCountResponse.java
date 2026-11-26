package news.bombom.notification.dto.response.stats;

import news.bombom.notification.domain.DevicePlatform;

public record PlatformOpenCountResponse(DevicePlatform platform, long count) {
}
