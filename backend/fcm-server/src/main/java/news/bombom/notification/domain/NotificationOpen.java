package news.bombom.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 사용자가 푸시 알림을 눌러 앱을 연 기록.
 * notificationId(회원별 발송) 또는 scheduleId(EVENT 토픽 발송)가 있으면 발송 건과 연결된 열림, 둘 다 없으면 미연결 열림이다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_open", indexes = {
        @Index(name = "idx_notification_open_opened_at", columnList = "opened_at"),
        @Index(name = "idx_notification_open_category_opened_at", columnList = "category, opened_at")
})
public class NotificationOpen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private NotificationCategory category;

    private Long notificationId;

    private Long scheduleId;

    private Long memberId;

    private String deviceUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DevicePlatform platform;

    @Column(length = 50)
    private String appVersion;

    @Column(nullable = false)
    private LocalDateTime openedAt;

    @Builder
    public NotificationOpen(
            NotificationCategory category,
            Long notificationId,
            Long scheduleId,
            Long memberId,
            String deviceUuid,
            @NonNull DevicePlatform platform,
            String appVersion,
            @NonNull LocalDateTime openedAt
    ) {
        this.category = category;
        this.notificationId = notificationId;
        this.scheduleId = scheduleId;
        this.memberId = memberId;
        this.deviceUuid = deviceUuid;
        this.platform = platform;
        this.appVersion = appVersion;
        this.openedAt = openedAt;
    }

    public boolean isLinked() {
        return notificationId != null || scheduleId != null;
    }
}
