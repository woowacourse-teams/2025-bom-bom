package news.bombom.event.service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
import news.bombom.notification.repository.NotificationStatsQueryRepository;
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
    private final NotificationStatsQueryRepository statsQueryRepository;
    
    private static final String EVENT_TOPIC = NotificationCategory.EVENT.getTopicName(); // "bombom_event"
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("M월 d일 (E) HH:mm", Locale.KOREA);

    @Transactional
    public void sendEventNotification(EventNotificationSchedule schedule) {
        Event event = eventService.getEvent(schedule.getEventId());
        String title = event.getName();
        String body = buildNotificationBody(event, schedule);

        Map<String, Object> data = Map.of(
                "eventId", event.getId().toString(),
                "notificationType", NotificationCategory.EVENT,
                "scheduleType", schedule.getType().name(),
                "scheduleId", String.valueOf(schedule.getId()),
                "category", NotificationCategory.EVENT.name()
        );
        Integer consentingMemberCount = countConsentingMembers(schedule);
        NotificationResult result = fcmNotificationSender.sendToTopic(EVENT_TOPIC, title, body, data, NotificationCategory.EVENT);
        if (result.isSuccess()) {
            schedule.markAsSent(consentingMemberCount);
            scheduleRepository.save(schedule);
            log.info("이벤트 알림 발송 성공: scheduleId={}, eventId={}, type={}", schedule.getId(), event.getId(), schedule.getType());
        } else {
            log.error("이벤트 알림 발송 실패: scheduleId={}, error={}", schedule.getId(), result.getErrorMessage());
            // best effort: 실패해도 예외는 전파하지 않음
        }
    }

    /**
     * 토픽 발송은 수신 대상 수를 알 수 없어 발송 직전의 수신 동의 회원 수를 지표용으로 남긴다.
     * 지표용 값이라 집계에 실패해도 발송은 막지 않는다.
     */
    private Integer countConsentingMembers(EventNotificationSchedule schedule) {
        try {
            return Math.toIntExact(statsQueryRepository.countConsentingMembersInNewTransaction(NotificationCategory.EVENT));
        } catch (RuntimeException e) {
            log.warn("이벤트 알림 수신 동의 회원 수 집계 실패: scheduleId={}, error={}", schedule.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * 알림 본문 생성
     */
    private String buildNotificationBody(Event event, EventNotificationSchedule schedule) {
        if (schedule.getType() == NotificationScheduleType.BEFORE_MINUTES) {
            String startTimeStr = event.getStartTime().format(DISPLAY_TIME_FORMATTER);
            return String.format("⏰ %d분 후 시작!\n시작 시각 %s", schedule.getMinutesBefore(), startTimeStr);
        } else {
            return "🎉 지금 시작!";
        }
    }
}
