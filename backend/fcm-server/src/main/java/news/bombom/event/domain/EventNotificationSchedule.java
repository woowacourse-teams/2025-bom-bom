package news.bombom.event.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import news.bombom.notification.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_notification_schedule")
public class EventNotificationSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationScheduleType type;

    private Integer minutesBefore;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean sent = false;

    private LocalDateTime sentAt;

    /**
     * 발송 시점의 이벤트 알림 수신 동의 회원 수. 토픽 발송은 수신 대상 수를 알 수 없어 열림률 분모(근사)로 쓴다.
     * 기록 전에 발송된 스케줄이나 집계에 실패하면 null이다.
     */
    private Integer consentingMemberCount;

    @Builder
    public EventNotificationSchedule(
            @NonNull Long eventId,
            @NonNull LocalDateTime scheduledAt,
            @NonNull NotificationScheduleType type,
            Integer minutesBefore
    ) {
        this.eventId = eventId;
        this.scheduledAt = scheduledAt;
        this.type = type;
        this.minutesBefore = minutesBefore;
        this.sent = false;
    }

    public void markAsSent(Integer consentingMemberCount) {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
        this.consentingMemberCount = consentingMemberCount;
    }
}
