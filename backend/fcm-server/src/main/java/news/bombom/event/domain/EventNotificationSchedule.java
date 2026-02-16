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

    public void markAsSent() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
    }
}
