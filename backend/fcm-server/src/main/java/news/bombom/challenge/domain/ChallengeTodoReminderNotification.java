package news.bombom.challenge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    private static final int MAX_RETRY_ATTEMPTS = 1;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private String challengeName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeTodoReminderPhase phase;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int streak;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isLastDay;

    @Builder
    public ChallengeTodoReminderNotification(
            @NonNull Long memberId,
            @NonNull Long challengeId,
            @NonNull String challengeName,
            ChallengeTodoReminderPhase phase,
            int streak,
            boolean isLastDay,
            NotificationStatus status,
            int attempts,
            LocalDateTime nextRetryAt,
            String lastError
    ) {
        super(memberId, status, attempts, nextRetryAt, lastError);
        this.challengeId = challengeId;
        this.challengeName = challengeName;
        this.phase = phase != null ? phase : ChallengeTodoReminderPhase.FIRST;
        this.streak = streak;
        this.isLastDay = isLastDay;
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
