package news.bombom.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import news.bombom.event.domain.Event;
import news.bombom.event.domain.EventNotificationSchedule;
import news.bombom.event.domain.EventStatus;
import news.bombom.event.domain.NotificationScheduleType;
import news.bombom.event.repository.EventNotificationScheduleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("이벤트 서비스 테스트")
class EventServiceTest {

    @Mock
    private EventNotificationScheduleRepository scheduleRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    @DisplayName("발송 대상 알림 예약 조회 성공")
    void getPendingSchedules_Success() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 2, 20, 13, 30);
        Event event = Event.builder()
                .name("테스트 이벤트")
                .startTime(LocalDateTime.of(2026, 2, 20, 14, 0))
                .status(EventStatus.SCHEDULED)
                .build();
        
        EventNotificationSchedule schedule1 = EventNotificationSchedule.builder()
                .eventId(1L)
                .scheduledAt(LocalDateTime.of(2026, 2, 20, 13, 30))
                .type(NotificationScheduleType.BEFORE_MINUTES)
                .minutesBefore(30)
                .build();
        
        EventNotificationSchedule schedule2 = EventNotificationSchedule.builder()
                .eventId(1L)
                .scheduledAt(LocalDateTime.of(2026, 2, 20, 13, 25))
                .type(NotificationScheduleType.BEFORE_MINUTES)
                .minutesBefore(35)
                .build();

        List<EventNotificationSchedule> expectedSchedules = List.of(schedule2, schedule1);
        when(scheduleRepository.findPendingSchedules(any(LocalDateTime.class)))
                .thenReturn(expectedSchedules);

        // when
        List<EventNotificationSchedule> result = eventService.getPendingSchedules();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(schedule2, schedule1);
        verify(scheduleRepository, times(1)).findPendingSchedules(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("발송 대상이 없으면 빈 리스트 반환")
    void getPendingSchedules_Empty_ReturnsEmptyList() {
        // given
        when(scheduleRepository.findPendingSchedules(any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        List<EventNotificationSchedule> result = eventService.getPendingSchedules();

        // then
        assertThat(result).isEmpty();
        verify(scheduleRepository, times(1)).findPendingSchedules(any(LocalDateTime.class));
    }
}
