package me.bombom.api.v1.inquiry.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.DiscordWebhookNotifier;
import me.bombom.api.v1.inquiry.dto.UnresolvedInquiryRoomCounts;
import me.bombom.api.v1.inquiry.service.InquiryRoomService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";

    private final InquiryRoomService inquiryRoomService;
    private final DiscordWebhookNotifier discordWebhookNotifier;

    @Scheduled(cron = "${inquiry.scheduler.daily-status-report.cron}", zone = TIME_ZONE)
    @SchedulerLock(name = "inquiry_daily_status_report", lockAtLeastFor = "PT4S", lockAtMostFor = "PT9S")
    public void sendDailyStatusReport() {
        log.info("문의 현황 디스코드 알림 발송 시작");
        UnresolvedInquiryRoomCounts statusCounts = inquiryRoomService.countUnresolvedRooms();
        discordWebhookNotifier.sendInquiryDailyStatusNotification(statusCounts);
        log.info("문의 현황 디스코드 알림 발송 완료");
    }
}
