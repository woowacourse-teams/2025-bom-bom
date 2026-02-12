package news.bombom.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleArrivalNotification extends Notification {

    private static final int MAX_RETRY_ATTEMPTS = 6;
    private static final int SHORT_RETRY_ATTEMPTS = 3;

    @Column(nullable = false)
    private Long articleId;

    @Column(nullable = false)
    private String newsletterName;

    @Column(nullable = false)
    private String articleTitle;

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
        super(memberId, status, attempts, nextRetryAt, lastError);
        this.id = id;
        this.articleId = articleId;
        this.newsletterName = newsletterName;
        this.articleTitle = articleTitle;
        this.isRead = isRead;
    }

    @Override
    public boolean shouldRetry() {
        return this.attempts <= MAX_RETRY_ATTEMPTS;
    }

    @Override
    public LocalDateTime calculateNextRetryTime(int attempts) {
        long delaySeconds;

        if (attempts <= SHORT_RETRY_ATTEMPTS) {
            delaySeconds = 30L * (long) Math.pow(2, attempts - 1);
        } else {
            switch (attempts) {
                case 4:
                    delaySeconds = 1800L;
                    break;
                case 5:
                    delaySeconds = 3600L;
                    break;
                case MAX_RETRY_ATTEMPTS:
                    delaySeconds = 10800L;
                    break;
                default:
                    delaySeconds = 10800L;
            }
        }

        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
