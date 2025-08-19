package me.bombom.api.v1.reading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyReading extends BaseEntity {

    private static final int INITIAL_GOAL_COUNT = 3;
    private static final int INITIAL_CURRENT_COUNT = 0;
    private static final int RESET_CURRENT_COUNT = 0;
    private static final int INCREASE_CURRENT_COUNT = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int goalCount;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int currentCount;

    @Builder
    public WeeklyReading(
            Long id,
            @NonNull Long memberId,
            int goalCount,
            int currentCount
    ) {
        this.id = id;
        this.memberId = memberId;
        this.goalCount = goalCount;
        this.currentCount = currentCount;
    }

    public static WeeklyReading create(Long memberId) {
        return WeeklyReading.builder()
                .memberId(memberId)
                .goalCount(INITIAL_GOAL_COUNT)
                .currentCount(INITIAL_CURRENT_COUNT)
                .build();
    }

    public void resetCurrentCount() {
        this.currentCount = RESET_CURRENT_COUNT;
    }

    public void updateGoalCount(int goalCount) {
        this.goalCount = goalCount;
    }

    public void increaseCurrentCount() {
        this.currentCount += INCREASE_CURRENT_COUNT;
    }
}
