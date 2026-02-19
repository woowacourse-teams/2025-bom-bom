package news.bombom.challenge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import news.bombom.notification.domain.Notification;
import news.bombom.notification.domain.NotificationStatus;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeTodoReminderNotification extends Notification {

    private static final int MAX_RETRY_ATTEMPTS = 4;

    @Column(nullable = false)
    private String challengeName;

    @Builder
    public ChallengeTodoReminderNotification(
            @NonNull Long memberId,
            @NonNull String challengeName,
            NotificationStatus status,
            int attempts,
            LocalDateTime nextRetryAt,
            String lastError
    ) {
        super(memberId, status, attempts, nextRetryAt, lastError);
        this.challengeName = challengeName;
    }

    @Override
    public boolean shouldRetry() {
        return this.attempts < MAX_RETRY_ATTEMPTS;
    }

    @Override
    public LocalDateTime calculateNextRetryTime(int attempts) {
        // 간단한 지수 백오프 (30s, 60s, 120s, 240s)
        long delaySeconds = 30L * (long) Math.pow(2, Math.max(0, attempts - 1));
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
