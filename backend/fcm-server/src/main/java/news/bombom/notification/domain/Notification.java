package news.bombom.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import news.bombom.notification.common.BaseEntity;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public abstract class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false)
    protected Long memberId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    protected NotificationStatus status;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    protected int attempts = 0;

    protected LocalDateTime nextRetryAt;

    @Column(length = 1024)
    protected String lastError;

    protected Notification(Long memberId, NotificationStatus status, int attempts,
                           LocalDateTime nextRetryAt, String lastError) {
        this.memberId = memberId;
        this.status = status != null ? status : NotificationStatus.PENDING;
        this.attempts = attempts;
        this.nextRetryAt = nextRetryAt;
        this.lastError = lastError;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.nextRetryAt = null;
        this.lastError = null;
    }

    public void markFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.attempts++;
        this.lastError = reason;
        this.nextRetryAt = calculateNextRetryTime(this.attempts);
    }

    /**
     * 기본은 재시도 없음. 필요한 경우 하위 클래스 override.
     */
    public boolean shouldRetry() {
        return attempts < getMaxRetryAttempts();
    }

    /**
     * 기본은 0회(재시도 안 함). 필요한 경우 하위 클래스 override.
     */
    protected int getMaxRetryAttempts() {
        return 0;
    }

    /**
     * 기본은 재시도 없음. 필요한 경우 하위 클래스 override.
     */
    public LocalDateTime calculateNextRetryTime(int attempts) {
        return null;
    }
}
