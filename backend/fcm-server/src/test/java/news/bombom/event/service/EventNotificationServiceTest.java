package news.bombom.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import news.bombom.event.domain.Event;
import news.bombom.event.domain.EventNotificationSchedule;
import news.bombom.event.domain.EventStatus;
import news.bombom.event.domain.NotificationScheduleType;
import news.bombom.event.repository.EventNotificationScheduleRepository;
import news.bombom.notification.client.firebase.FcmNotificationSender;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.dto.NotificationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("이벤트 알림 서비스 테스트")
class EventNotificationServiceTest {

    private static final String EVENT_TOPIC = NotificationCategory.EVENT.getTopicName();
    private static final Long EVENT_ID = 1L;
    private static final Long SCHEDULE_ID = 10L;
    private static final String EVENT_NAME = "봄봄 이벤트";
    private static final LocalDateTime EVENT_START_TIME = LocalDateTime.of(2026, 2, 20, 14, 0);

    @Mock
    private EventNotificationScheduleRepository scheduleRepository;

    @Mock
    private EventService eventService;

    @Mock
    private FcmNotificationSender fcmNotificationSender;

    @InjectMocks
    private EventNotificationService eventNotificationService;

    @Test
    @DisplayName("이벤트 알림 발송 성공 - N분 전 알림")
    void sendEventNotification_Success_BeforeMinutes() {
        // given
        Event event = createEvent(EventStatus.SCHEDULED);
        EventNotificationSchedule schedule = createSchedule(NotificationScheduleType.BEFORE_MINUTES, 30);
        
        when(eventService.getEvent(EVENT_ID)).thenReturn(event);
        when(fcmNotificationSender.sendToTopic(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(NotificationResult.success("message-id-123"));
        when(scheduleRepository.save(any(EventNotificationSchedule.class))).thenReturn(schedule);

        // when
        eventNotificationService.sendEventNotification(schedule);

        // then
        assertThat(schedule.isSent()).isTrue();
        assertThat(schedule.getSentAt()).isNotNull();
        verify(eventService, times(1)).getEvent(EVENT_ID);
        verify(fcmNotificationSender, times(1)).sendToTopic(
                eq(EVENT_TOPIC),
                eq(EVENT_NAME),
                anyString(),
                anyMap()
        );
        verify(scheduleRepository, times(1)).save(schedule);
    }

    @Test
    @DisplayName("이벤트 알림 발송 성공 - 시작 시 알림")
    void sendEventNotification_Success_AtStart() {
        // given
        Event event = createEvent(EventStatus.SCHEDULED);
        EventNotificationSchedule schedule = createSchedule(NotificationScheduleType.AT_START, null);
        
        when(eventService.getEvent(EVENT_ID)).thenReturn(event);
        when(fcmNotificationSender.sendToTopic(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(NotificationResult.success("message-id-123"));
        when(scheduleRepository.save(any(EventNotificationSchedule.class))).thenReturn(schedule);

        // when
        eventNotificationService.sendEventNotification(schedule);

        // then
        assertThat(schedule.isSent()).isTrue();
        verify(fcmNotificationSender, times(1)).sendToTopic(
                eq(EVENT_TOPIC),
                eq(EVENT_NAME),
                anyString(),
                anyMap()
        );
        verify(scheduleRepository, times(1)).save(schedule);
    }

    @Test
    @DisplayName("이벤트를 찾을 수 없으면 예외 발생")
    void sendEventNotification_EventNotFound_ThrowsException() {
        // given
        EventNotificationSchedule schedule = createSchedule(NotificationScheduleType.AT_START, null);
        when(eventService.getEvent(EVENT_ID))
                .thenThrow(new IllegalArgumentException("이벤트를 찾을 수 없습니다: " + EVENT_ID));

        // when & then
        assertThatThrownBy(() -> eventNotificationService.sendEventNotification(schedule))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이벤트를 찾을 수 없습니다");
        
        verify(fcmNotificationSender, never()).sendToTopic(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("FCM 발송 실패 시 예외를 전파하지 않음 (Best effort)")
    void sendEventNotification_FcmFailure_DoesNotPropagate() {
        // given
        Event event = createEvent(EventStatus.SCHEDULED);
        EventNotificationSchedule schedule = createSchedule(NotificationScheduleType.AT_START, null);
        
        when(eventService.getEvent(EVENT_ID)).thenReturn(event);
        when(fcmNotificationSender.sendToTopic(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(NotificationResult.failure("FCM 발송 실패"));

        // when & then
        eventNotificationService.sendEventNotification(schedule);

        // then
        assertThat(schedule.isSent()).isFalse();
        verify(fcmNotificationSender, times(1)).sendToTopic(anyString(), anyString(), anyString(), anyMap());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("알림 본문 생성 - N분 전 알림")
    void buildNotificationBody_BeforeMinutes_ContainsMinutes() {
        // given
        Event event = createEvent(EventStatus.SCHEDULED);
        EventNotificationSchedule schedule = createSchedule(NotificationScheduleType.BEFORE_MINUTES, 30);
        
        when(eventService.getEvent(EVENT_ID)).thenReturn(event);
        when(fcmNotificationSender.sendToTopic(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(NotificationResult.success("message-id"));

        // when
        eventNotificationService.sendEventNotification(schedule);

        // then
        verify(fcmNotificationSender).sendToTopic(
                eq(EVENT_TOPIC),
                eq(EVENT_NAME),
                org.mockito.ArgumentMatchers.argThat(body -> 
                        body.contains("30분 후 시작됩니다") && body.contains("2026-02-20 14:00")
                ),
                anyMap()
        );
    }

    @Test
    @DisplayName("알림 본문 생성 - 시작 시 알림")
    void buildNotificationBody_AtStart_ContainsStartMessage() {
        // given
        Event event = createEvent(EventStatus.SCHEDULED);
        EventNotificationSchedule schedule = createSchedule(NotificationScheduleType.AT_START, null);
        
        when(eventService.getEvent(EVENT_ID)).thenReturn(event);
        when(fcmNotificationSender.sendToTopic(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(NotificationResult.success("message-id"));

        // when
        eventNotificationService.sendEventNotification(schedule);

        // then
        verify(fcmNotificationSender).sendToTopic(
                eq(EVENT_TOPIC),
                eq(EVENT_NAME),
                org.mockito.ArgumentMatchers.argThat(body -> 
                        body.contains("시작되었습니다") && body.contains("2026-02-20 14:00")
                ),
                anyMap()
        );
    }

    @Test
    @DisplayName("알림 데이터에 eventId와 scheduleType 포함")
    void sendEventNotification_DataContainsEventIdAndScheduleType() {
        // given
        Event event = createEvent(EventStatus.SCHEDULED);
        EventNotificationSchedule schedule = createSchedule(NotificationScheduleType.BEFORE_MINUTES, 30);
        
        when(eventService.getEvent(EVENT_ID)).thenReturn(event);
        when(fcmNotificationSender.sendToTopic(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(NotificationResult.success("message-id"));

        // when
        eventNotificationService.sendEventNotification(schedule);

        // then
        verify(fcmNotificationSender).sendToTopic(
                eq(EVENT_TOPIC),
                anyString(),
                anyString(),
                org.mockito.ArgumentMatchers.argThat(data -> {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    return dataMap.containsKey("eventId") 
                            && dataMap.containsKey("notificationType")
                            && dataMap.containsKey("scheduleType")
                            && dataMap.get("eventId").equals(EVENT_ID.toString())
                            && dataMap.get("scheduleType").equals("BEFORE_MINUTES");
                })
        );
    }

    private Event createEvent(EventStatus status) {
        Event event = Event.builder()
                .name(EVENT_NAME)
                .startTime(EVENT_START_TIME)
                .status(status)
                .build();
        // ID 설정을 위해 리플렉션 사용 (테스트용)
        try {
            java.lang.reflect.Field idField = Event.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(event, EVENT_ID);
        } catch (Exception e) {
            // 테스트용이므로 무시
        }
        return event;
    }

    private EventNotificationSchedule createSchedule(NotificationScheduleType type, Integer minutesBefore) {
        EventNotificationSchedule schedule = EventNotificationSchedule.builder()
                .eventId(EVENT_ID)
                .scheduledAt(EVENT_START_TIME.minusMinutes(minutesBefore != null ? minutesBefore : 0))
                .type(type)
                .minutesBefore(minutesBefore)
                .build();
        // ID 설정을 위해 리플렉션 사용 (테스트용)
        try {
            java.lang.reflect.Field idField = EventNotificationSchedule.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(schedule, SCHEDULE_ID);
        } catch (Exception e) {
            // 테스트용이므로 무시
        }
        return schedule;
    }
}
