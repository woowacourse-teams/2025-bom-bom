package me.bombom.api.v1.subscribe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscribeRetry extends BaseEntity {

    private static final int MAX_RETRY_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long subscribeId;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private LocalDateTime nextRetryAt;

    private String lastError;

    @Builder
    public SubscribeRetry(
            Long id,
            @NonNull Long subscribeId,
            @NonNull LocalDateTime nextRetryAt,
            String lastError
    ) {
        this.id = id;
        this.subscribeId = subscribeId;
        this.nextRetryAt = nextRetryAt;
        this.lastError = lastError;
        this.retryCount = 0;
    }

    public void increaseRetryCount(LocalDateTime now, String errorMsg) {
        this.nextRetryAt = calculateNextRetryTime(now);
        this.retryCount += 1;
        this.lastError = errorMsg;
    }

    private LocalDateTime calculateNextRetryTime(LocalDateTime now) {
        if (this.retryCount == 0) {
            return now.plusMinutes(10);
        }

        long hoursToAdd = switch (this.retryCount) {
            case 1 -> 1; // 1시간
            case 2 -> 6; // 6시간
            default -> 12; // 12시간
        };
        return now.plusHours(hoursToAdd);
    }

    public boolean isMaxRetryReached() {
        return this.retryCount >= MAX_RETRY_COUNT;
    }
}
