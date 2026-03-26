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
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeTodoReminderNotification extends BaseEntity {

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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeTodoReminderPhase phase;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int streak;

    private Integer daysAbsent;

    private Integer remainingAbsences;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isLastDay;

    @Builder
    public ChallengeTodoReminderNotification(
            @NonNull Long memberId,
            @NonNull NotificationStatus status,
            int attempts,
            LocalDateTime nextRetryAt,
            String lastError,
            @NonNull Long challengeId,
            @NonNull String challengeName,
            @NonNull ChallengeTodoReminderPhase phase,
            int streak,
            Integer daysAbsent,
            Integer remainingAbsences,
            boolean isLastDay
    ) {
        this.memberId = memberId;
        this.status = status;
        this.attempts = attempts;
        this.nextRetryAt = nextRetryAt;
        this.lastError = lastError;
        this.challengeId = challengeId;
        this.challengeName = challengeName;
        this.phase = phase;
        this.streak = streak;
        this.daysAbsent = daysAbsent;
        this.remainingAbsences = remainingAbsences;
        this.isLastDay = isLastDay;
    }

    public static ChallengeTodoReminderNotification createPending(
            Long memberId,
            Long challengeId,
            String challengeName,
            ChallengeTodoReminderPhase phase,
            int streak,
            Integer daysAbsent,
            Integer remainingAbsences,
            boolean isLastDay
    ) {
        return ChallengeTodoReminderNotification.builder()
                .memberId(memberId)
                .status(NotificationStatus.PENDING)
                .attempts(0)
                .challengeId(challengeId)
                .challengeName(challengeName)
                .phase(phase)
                .streak(streak)
                .daysAbsent(daysAbsent)
                .remainingAbsences(remainingAbsences)
                .isLastDay(isLastDay)
                .build();
    }
}
