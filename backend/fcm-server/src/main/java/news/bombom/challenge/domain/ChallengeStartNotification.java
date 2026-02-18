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
public class ChallengeStartNotification extends Notification {

    private static final int MAX_RETRY_ATTEMPTS = 4;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private String challengeName;

    @Builder
    public ChallengeStartNotification(
            Long id,
            @NonNull Long memberId,
            @NonNull Long challengeId,
            @NonNull String challengeName,
            NotificationStatus status,
            int attempts,
            LocalDateTime nextRetryAt,
            String lastError
    ) {
        super(memberId, status, attempts, nextRetryAt, lastError);
        this.id = id;
        this.challengeId = challengeId;
        this.challengeName = challengeName;
    }

    @Override
    public boolean shouldRetry() {
        return this.attempts < MAX_RETRY_ATTEMPTS;
    }

    @Override
    public LocalDateTime calculateNextRetryTime(int attempts) {
        long delaySeconds = 30L * (long) Math.pow(2, Math.max(0, attempts - 1));
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
