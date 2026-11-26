package news.bombom.notification.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import news.bombom.notification.config.AdminApiKeyVerifier;
import news.bombom.notification.dto.response.stats.NotificationStatsResponse;
import news.bombom.notification.service.NotificationStatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications/admin")
public class NotificationAdminStatsController {

    private final AdminApiKeyVerifier adminApiKeyVerifier;
    private final NotificationStatsService notificationStatsService;

    @GetMapping("/stats")
    public NotificationStatsResponse getStats(
            @RequestHeader(value = AdminApiKeyVerifier.HEADER_NAME, required = false) String apiKey,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        adminApiKeyVerifier.verify(apiKey);
        return notificationStatsService.getStats(from, to);
    }
}
