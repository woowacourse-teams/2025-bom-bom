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
import news.bombom.notification.domain.SendResult.SendStatus;

/**
 * 회원별 알림 1건을 기기 단위로 발송한 결과. 알림 row는 회원 단위 상태만 남기므로 기기별 결과는 여기에 기록한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_device_send_log", indexes = {
        @Index(name = "idx_notification_device_send_log_sent_at", columnList = "sent_at"),
        @Index(name = "idx_notification_device_send_log_category_sent_at", columnList = "category, sent_at")
})
public class NotificationDeviceSendLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private NotificationCategory category;

    private Long notificationId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String deviceUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SendStatus status;

    @Column(length = 64)
    private String errorCode;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Builder
    public NotificationDeviceSendLog(
            NotificationCategory category,
            Long notificationId,
            @NonNull Long memberId,
            @NonNull String deviceUuid,
            @NonNull SendStatus status,
            String errorCode,
            @NonNull LocalDateTime sentAt
    ) {
        this.category = category;
        this.notificationId = notificationId;
        this.memberId = memberId;
        this.deviceUuid = deviceUuid;
        this.status = status;
        this.errorCode = errorCode;
        this.sentAt = sentAt;
    }
}
