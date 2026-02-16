package news.bombom.event.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import news.bombom.event.domain.Event;
import news.bombom.event.domain.EventNotificationSchedule;
import news.bombom.event.domain.EventStatus;
import news.bombom.event.domain.NotificationScheduleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("이벤트 알림 스케줄 Repository 테스트")
class EventNotificationScheduleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventNotificationScheduleRepository scheduleRepository;

    @Autowired
    private EventRepository eventRepository;

    private Event scheduledEvent;
    private Event cancelledEvent;

    @BeforeEach
    void setUp() {
        scheduledEvent = Event.builder()
                .name("예정된 이벤트")
                .startTime(LocalDateTime.of(2026, 2, 20, 14, 0))
                .status(EventStatus.SCHEDULED)
                .build();
        scheduledEvent = entityManager.persistAndFlush(scheduledEvent);

        cancelledEvent = Event.builder()
                .name("취소된 이벤트")
                .startTime(LocalDateTime.of(2026, 2, 20, 15, 0))
                .status(EventStatus.CANCELLED)
                .build();
        cancelledEvent = entityManager.persistAndFlush(cancelledEvent);
    }

    @Test
    @DisplayName("발송 대상 조회 - SCHEDULED 상태 이벤트의 미발송 알림만 조회")
    void findPendingSchedules_OnlyScheduledEvents_ReturnsPendingSchedules() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 2, 20, 13, 30);
        
        // 발송 대상 (SCHEDULED 이벤트, 미발송)
        EventNotificationSchedule pending1 = EventNotificationSchedule.builder()
                .eventId(scheduledEvent.getId())
                .scheduledAt(LocalDateTime.of(2026, 2, 20, 13, 25))
                .type(NotificationScheduleType.BEFORE_MINUTES)
                .minutesBefore(35)
                .build();
        entityManager.persistAndFlush(pending1);

        EventNotificationSchedule pending2 = EventNotificationSchedule.builder()
                .eventId(scheduledEvent.getId())
                .scheduledAt(LocalDateTime.of(2026, 2, 20, 13, 30))
                .type(NotificationScheduleType.BEFORE_MINUTES)
                .minutesBefore(30)
                .build();
        entityManager.persistAndFlush(pending2);

        // 이미 발송된 알림 (제외되어야 함)
        EventNotificationSchedule sent = EventNotificationSchedule.builder()
                .eventId(scheduledEvent.getId())
                .scheduledAt(LocalDateTime.of(2026, 2, 20, 13, 20))
                .type(NotificationScheduleType.BEFORE_MINUTES)
                .minutesBefore(40)
                .build();
        sent.markAsSent();
        entityManager.persistAndFlush(sent);

        // 취소된 이벤트의 알림 (제외되어야 함)
        EventNotificationSchedule cancelledEventSchedule = EventNotificationSchedule.builder()
                .eventId(cancelledEvent.getId())
                .scheduledAt(LocalDateTime.of(2026, 2, 20, 14, 55))
                .type(NotificationScheduleType.BEFORE_MINUTES)
                .minutesBefore(5)
                .build();
        entityManager.persistAndFlush(cancelledEventSchedule);

        // 미래 시각 알림 (제외되어야 함)
        EventNotificationSchedule future = EventNotificationSchedule.builder()
                .eventId(scheduledEvent.getId())
                .scheduledAt(LocalDateTime.of(2026, 2, 20, 13, 35))
                .type(NotificationScheduleType.BEFORE_MINUTES)
                .minutesBefore(25)
                .build();
        entityManager.persistAndFlush(future);

        entityManager.clear();

        // when
        List<EventNotificationSchedule> result = scheduleRepository.findPendingSchedules(now);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(EventNotificationSchedule::getId)
                .containsExactly(pending1.getId(), pending2.getId());
        assertThat(result).allMatch(schedule -> !schedule.isSent());
        assertThat(result).allMatch(schedule -> schedule.getEventId().equals(scheduledEvent.getId()));
    }

    @Test
    @DisplayName("발송 대상 조회 - scheduledAt 기준 오름차순 정렬")
    void findPendingSchedules_OrderedByScheduledAt() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 2, 20, 13, 30);
        
        EventNotificationSchedule later = EventNotificationSchedule.builder()
                .eventId(scheduledEvent.getId())
                .scheduledAt(LocalDateTime.of(2026, 2, 20, 13, 30))
                .type(NotificationScheduleType.BEFORE_MINUTES)
                .minutesBefore(30)
                .build();
        entityManager.persistAndFlush(later);

        EventNotificationSchedule earlier = EventNotificationSchedule.builder()
                .eventId(scheduledEvent.getId())
                .scheduledAt(LocalDateTime.of(2026, 2, 20, 13, 25))
                .type(NotificationScheduleType.BEFORE_MINUTES)
                .minutesBefore(35)
                .build();
        entityManager.persistAndFlush(earlier);

        entityManager.clear();

        // when
        List<EventNotificationSchedule> result = scheduleRepository.findPendingSchedules(now);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getScheduledAt()).isBefore(result.get(1).getScheduledAt());
    }

    @Test
    @DisplayName("발송 대상 조회 - 대상이 없으면 빈 리스트 반환")
    void findPendingSchedules_NoPending_ReturnsEmptyList() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 2, 20, 13, 0);

        // when
        List<EventNotificationSchedule> result = scheduleRepository.findPendingSchedules(now);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("발송 대상 조회 - 정확히 scheduledAt 시각인 알림도 포함")
    void findPendingSchedules_ExactTime_IncludesSchedule() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 2, 20, 13, 30);
        
        EventNotificationSchedule exact = EventNotificationSchedule.builder()
                .eventId(scheduledEvent.getId())
                .scheduledAt(now)
                .type(NotificationScheduleType.BEFORE_MINUTES)
                .minutesBefore(30)
                .build();
        entityManager.persistAndFlush(exact);

        entityManager.clear();

        // when
        List<EventNotificationSchedule> result = scheduleRepository.findPendingSchedules(now);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScheduledAt()).isEqualTo(now);
    }
}
