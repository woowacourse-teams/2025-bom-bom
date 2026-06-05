package me.bombom.api.v1.reading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_continue_reading_shield_member_id", columnNames = "member_id")
})
public class ContinueReadingShield extends BaseEntity {

    private static final int INITIAL_MONTHLY_REMAINING_COUNT = 1;
    private static final int INITIAL_REWARD_REMAINING_COUNT = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int monthlyRemainingCount;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int rewardRemainingCount;

    @Builder
    public ContinueReadingShield(
            Long id,
            @NonNull Long memberId,
            int monthlyRemainingCount,
            int rewardRemainingCount
    ) {
        this.id = id;
        this.memberId = memberId;
        this.monthlyRemainingCount = Math.max(monthlyRemainingCount, 0);
        this.rewardRemainingCount = Math.max(rewardRemainingCount, 0);
    }

    public static ContinueReadingShield create(Long memberId) {
        return ContinueReadingShield.builder()
                .memberId(memberId)
                .monthlyRemainingCount(INITIAL_MONTHLY_REMAINING_COUNT)
                .rewardRemainingCount(INITIAL_REWARD_REMAINING_COUNT)
                .build();
    }

    public int getRemainingCount() {
        return monthlyRemainingCount + rewardRemainingCount;
    }
}
