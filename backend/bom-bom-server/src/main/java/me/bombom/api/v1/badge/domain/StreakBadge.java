package me.bombom.api.v1.badge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue("STREAK")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StreakBadge extends Badge {

    @Column(name = "streak_day_count")
    private Integer streakDayCount;

    @Builder
    public StreakBadge(
            Long id,
            Long memberId,
            Integer streakDayCount
    ) {
        super(id, memberId, BadgeCategory.STREAK);
        this.streakDayCount = streakDayCount;
    }

    public StreakBadgeTier getTier() {
        return StreakBadgeTier.from(streakDayCount)
                .orElseThrow(() -> new IllegalStateException("유효하지 않은 스트릭 뱃지 일수입니다. streakDayCount=" + streakDayCount));
    }
}
