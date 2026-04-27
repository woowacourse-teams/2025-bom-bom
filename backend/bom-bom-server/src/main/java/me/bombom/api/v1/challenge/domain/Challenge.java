package me.bombom.api.v1.challenge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;
import me.bombom.api.v1.common.util.DateUtils;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseEntity {

    private static final double LATE_REGISTRATION_ALLOWED_RATIO = 0.2;
    private static final double SUCCESS_REQUIRED_RATIO = 0.8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int generation;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private int totalDays;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isBadgeIssued = false;

    @Column(nullable = false)
    private Long newsletterGroupId;

    @Builder
    public Challenge(
            Long id,
            @NonNull String name,
            int generation,
            LocalDate startDate,
            LocalDate endDate,
            int totalDays,
            boolean isBadgeIssued,
            @NonNull Long newsletterGroupId
    ) {
        this.id = id;
        this.name = name;
        this.generation = generation;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = totalDays;
        this.isBadgeIssued = isBadgeIssued;
        this.newsletterGroupId = newsletterGroupId;
    }

    public void markBadgeAsIssued() {
        this.isBadgeIssued = true;
    }

    public ChallengeStatus getStatus(LocalDate now) {
        if (this.startDate == null) {
            return ChallengeStatus.COMING_SOON;
        }
        if (now.isBefore(this.startDate)) {
            return ChallengeStatus.BEFORE_START;
        }
        if (this.endDate != null && now.isAfter(this.endDate)) {
            return ChallengeStatus.COMPLETED;
        }
        return ChallengeStatus.ONGOING;
    }

    public boolean isEnded(LocalDate now) {
        return this.endDate != null && now.isAfter(this.endDate);
    }

    public boolean isLastDay(LocalDate date) {
        return date.equals(this.endDate);
    }

    public int calculateMaxAllowedAbsences() {
        return totalDays - (int) Math.ceil(totalDays * SUCCESS_REQUIRED_RATIO);
    }

    public boolean hasStarted(LocalDate now) {
        return this.startDate != null && !now.isBefore(this.startDate);
    }

    public RegistrationPhase getRegistrationPhase(LocalDate now) {
        if (!hasStarted(now)) {
            return RegistrationPhase.EARLY;
        }
        if (isWithinLatePhase(now)) {
            return RegistrationPhase.LATE;
        }
        return RegistrationPhase.CLOSED;
    }

    public boolean isRegistrationClosed(LocalDate now) {
        return getRegistrationPhase(now) == RegistrationPhase.CLOSED;
    }

    public boolean isLatePhase(LocalDate now) {
        return getRegistrationPhase(now) == RegistrationPhase.LATE;
    }

    public int calculatePassedWeekDays(LocalDate targetDate) {
        if (this.startDate == null) {
            return 0;
        }
        int passedDays = 0;
        LocalDate currentDate = this.startDate;
        while (!currentDate.isAfter(targetDate)) {
            if (isWeekday(currentDate)) {
                passedDays++;
            }
            currentDate = currentDate.plusDays(1);
        }
        return passedDays;
    }

    private boolean isWeekday(LocalDate currentDate) {
        return !DateUtils.isWeekend(currentDate);
    }

    private boolean isWithinLatePhase(LocalDate targetDate) {
        int passedWeekDays = calculatePassedWeekDays(targetDate);
        int maxPassedDaysForLateRegistration = (int) (this.totalDays * LATE_REGISTRATION_ALLOWED_RATIO);
        return passedWeekDays <= maxPassedDaysForLateRegistration;
    }
}
