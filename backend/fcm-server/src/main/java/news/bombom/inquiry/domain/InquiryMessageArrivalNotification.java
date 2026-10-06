package news.bombom.inquiry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
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

    @Column(nullable = false)
    private Long messageId;

    // DB 컬럼 아님: 발송 시점에 messageId로 조회한 roomId를 잠깐 들고 있는 값 (Processor가 채워줌)
    @Transient
    private Long roomId;

    // DB 컬럼 아님: 발송 시점에 messageId로 조회한 content를 잠깐 들고 있는 값 (Processor가 채워줌)
    @Transient
    private String content;

    @Builder
    public InquiryMessageArrivalNotification(
            @NonNull Long memberId,
            @NonNull Long messageId,
            NotificationStatus status,
            int attempts,
            LocalDateTime nextRetryAt,
            String lastError
    ) {
        super(memberId, status, attempts, nextRetryAt, lastError);
        this.messageId = messageId;
    }

    public void assignRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public void assignContent(String content) {
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
