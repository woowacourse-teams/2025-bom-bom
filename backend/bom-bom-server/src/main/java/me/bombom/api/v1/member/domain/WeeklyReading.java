package me.bombom.api.v1.member.domain;

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

    public void updateGoalCount(int goalCount) {
        this.goalCount = goalCount;
    }

    public void increaseCurrentCount() {
        this.currentCount += INCREASE_CURRENT_COUNT;
    }
}
