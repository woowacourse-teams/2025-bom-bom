package me.bombom.api.v1.challenge.domain.notification;

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
import me.bombom.api.v1.challenge.domain.NotificationStatus;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeStartNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @Column(nullable = false)
    private int attempts;

    private LocalDateTime nextRetryAt;

    @Column(length = 1024)
    private String lastError;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private String challengeName;

    @Builder
    public ChallengeStartNotification(
            Long id,
            @NonNull Long memberId,
            @NonNull NotificationStatus status,
            int attempts,
            LocalDateTime nextRetryAt,
            String lastError,
            @NonNull Long challengeId,
            @NonNull String challengeName
    ) {
        this.id = id;
        this.memberId = memberId;
        this.status = status;
        this.attempts = attempts;
        this.nextRetryAt = nextRetryAt;
        this.lastError = lastError;
        this.challengeId = challengeId;
        this.challengeName = challengeName;
    }

    public static ChallengeStartNotification createPending(
            Long memberId,
            Long challengeId,
            String challengeName
    ) {
        return ChallengeStartNotification.builder()
                .memberId(memberId)
                .status(NotificationStatus.PENDING)
                .attempts(0)
                .challengeId(challengeId)
                .challengeName(challengeName)
                .build();
    }
}
