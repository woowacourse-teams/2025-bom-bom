package news.bombom.event.service;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.event.domain.Event;
import news.bombom.event.domain.EventNotificationSchedule;
import news.bombom.event.domain.NotificationScheduleType;
import news.bombom.event.repository.EventNotificationScheduleRepository;
import news.bombom.notification.client.firebase.FcmNotificationSender;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.dto.NotificationResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventNotificationService {

    private final EventNotificationScheduleRepository scheduleRepository;
    private final EventService eventService;
    private final FcmNotificationSender fcmNotificationSender;
    
    private static final String EVENT_TOPIC = NotificationCategory.EVENT.getTopicName(); // "bombom_event"
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional
    public void sendEventNotification(EventNotificationSchedule schedule) {
        Event event = eventService.getEvent(schedule.getEventId());
        String title = event.getName();
        String body = buildNotificationBody(event, schedule);

        Map<String, Object> data = Map.of(
                "eventId", event.getId().toString(),
                "notificationType", "eventNotification",
                "scheduleType", schedule.getType().name()
        );
        NotificationResult result = fcmNotificationSender.sendToTopic(EVENT_TOPIC, title, body, data);
        if (result.isSuccess()) {
            schedule.markAsSent();
            scheduleRepository.save(schedule);
            log.info("이벤트 알림 발송 성공: scheduleId={}, eventId={}, type={}", schedule.getId(), event.getId(), schedule.getType());
        } else {
            log.error("이벤트 알림 발송 실패: scheduleId={}, error={}", schedule.getId(), result.getErrorMessage());
            // best effort: 실패해도 예외는 전파하지 않음
        }
    }

    /**
     * 알림 본문 생성
     */
    private String buildNotificationBody(Event event, EventNotificationSchedule schedule) {
        String startTimeStr = event.getStartTime().format(TIME_FORMATTER);
        if (schedule.getType() == NotificationScheduleType.BEFORE_MINUTES) {
            return String.format("이벤트가 %d분 후 시작됩니다. (%s)", 
                    schedule.getMinutesBefore(), startTimeStr);
        } else {
            return String.format("이벤트가 시작되었습니다! (%s)", startTimeStr);
        }
    }
}
