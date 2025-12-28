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

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int totalDays;

    @Builder
    public Challenge(
            Long id,
            @NonNull String name,
            int generation,
            @NonNull LocalDate startDate,
            @NonNull LocalDate endDate,
            int totalDays
    ) {
        this.id = id;
        this.name = name;
        this.generation = generation;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = totalDays;
    }

    public ChallengeStatus getStatus(LocalDate now) {
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
}
