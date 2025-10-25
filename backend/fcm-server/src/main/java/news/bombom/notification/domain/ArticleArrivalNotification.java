package news.bombom.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import news.bombom.notification.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleArrivalNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long articleId;

    @Column(nullable = false)
    private String newsletterName;

    @Column(nullable = false)
    private String articleTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int attempts = 0;

    private LocalDateTime nextRetryAt;

    @Column(length = 1024)
    private String lastError;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isRead = false;

    @Builder
    public ArticleArrivalNotification(
            Long id,
            @NonNull Long memberId,
            @NonNull Long articleId,
            @NonNull String newsletterName,
            @NonNull String articleTitle,
            NotificationStatus status,
            int attempts,
            LocalDateTime nextRetryAt,
            String lastError,
            boolean isRead
    ) {
        this.id = id;
        this.memberId = memberId;
        this.articleId = articleId;
        this.newsletterName = newsletterName;
        this.articleTitle = articleTitle;
        this.status = status != null ? status : NotificationStatus.PENDING;
        this.attempts = attempts;
        this.nextRetryAt = nextRetryAt;
        this.lastError = lastError;
        this.isRead = isRead;
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

    public void resetForRetry() {
        this.status = NotificationStatus.PENDING;
        this.nextRetryAt = null;
        this.lastError = null;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    private LocalDateTime calculateNextRetryTime(int attempts) {
        long delaySeconds = 30L * (long) Math.pow(2, attempts - 1); // 30s, 60s, 120s...
        if (delaySeconds > 1800) { // Max 30 minutes
            delaySeconds = 1800;
        }
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
