package me.bombom.api.v1.challenge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseEntity {

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

    @Builder
    public Challenge(
            Long id,
            @NonNull String name,
            int generation,
            LocalDate startDate,
            LocalDate endDate,
            int totalDays,
            boolean isBadgeIssued
    ) {
        this.id = id;
        this.name = name;
        this.generation = generation;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = totalDays;
        this.isBadgeIssued = isBadgeIssued;
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
        if (now.isAfter(this.endDate)) {
            return ChallengeStatus.COMPLETED;
        }
        return ChallengeStatus.ONGOING;
    }

    public boolean isEnded(LocalDate now) {
        return now.isAfter(this.endDate);
    }

    public boolean hasStarted(LocalDate now) {
        return !now.isBefore(this.startDate);
    }

    public int calculatePassedDays(LocalDate targetDate) {
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
        DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
}
