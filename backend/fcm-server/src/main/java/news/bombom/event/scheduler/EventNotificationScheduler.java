package news.bombom.event.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.event.domain.EventNotificationSchedule;
import news.bombom.event.service.EventService;
import news.bombom.event.service.EventNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventNotificationScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final int PER_MINUTE = 60000;

    private final EventService eventService;
    private final EventNotificationService notificationService;

    @Scheduled(fixedDelay = PER_MINUTE, zone = TIME_ZONE)
    public void processPendingNotifications() {
        try {
            List<EventNotificationSchedule> pendingSchedules = eventService.getPendingSchedules();
            if (pendingSchedules.isEmpty()) {
                return;
            }

            log.info("발송 대상 알림 개수: {}", pendingSchedules.size());
            for (EventNotificationSchedule schedule : pendingSchedules) {
                notificationService.sendEventNotification(schedule);
            }
        } catch (Exception e) {
            log.error("이벤트 알림 스케줄러 실행 중 오류 발생", e);
        }
    }
}
