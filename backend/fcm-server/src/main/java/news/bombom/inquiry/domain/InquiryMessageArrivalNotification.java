package news.bombom.inquiry.domain;

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
public class InquiryMessageArrivalNotification extends Notification {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int CONTENT_MAX_LENGTH = 20;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false, length = CONTENT_MAX_LENGTH)
    private String content;

    @Builder
    public InquiryMessageArrivalNotification(
            @NonNull Long memberId,
            @NonNull Long roomId,
            @NonNull String content,
            NotificationStatus status,
            int attempts,
            LocalDateTime nextRetryAt,
            String lastError
    ) {
        super(memberId, status, attempts, nextRetryAt, lastError);
        this.roomId = roomId;
        this.content = content;
    }

    @Override
    public boolean shouldRetry() {
        return this.attempts < MAX_RETRY_ATTEMPTS;
    }

    @Override
    public LocalDateTime calculateNextRetryTime(int attempts) {
        // 재시도 정책: 3회, 30초 -> 2분 -> 5분
        long delaySeconds = switch (attempts) {
            case 1 -> 30L;
            case 2 -> 120L;
            default -> 300L;
        };
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
