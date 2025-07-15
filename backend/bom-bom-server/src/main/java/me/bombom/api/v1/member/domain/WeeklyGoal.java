package me.bombom.api.v1.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;
import org.hibernate.validator.constraints.UniqueElements;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyGoal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int weeklyGoalCount;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int currentCount;

    @Builder
    public WeeklyGoal(
            Long id,
            @NonNull Long memberId,
            int weeklyGoalCount,
            int currentCount
    ) {
        this.id = id;
        this.memberId = memberId;
        this.weeklyGoalCount = weeklyGoalCount;
        this.currentCount = currentCount;
    }

    public void updateWeeklyGoalCount(int goalCount) {
        this.weeklyGoalCount = goalCount;
    }
}
