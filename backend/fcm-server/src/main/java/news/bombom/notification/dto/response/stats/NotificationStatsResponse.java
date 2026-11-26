package news.bombom.notification.dto.response.stats;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record NotificationStatsResponse(
        OffsetDateTime aggregatedAt,
        LocalDate from,
        LocalDate to,
        List<CategoryStatsResponse> categories,
        ReachResponse reach,
        List<PlatformOpenCountResponse> opensByPlatform
) {
}
