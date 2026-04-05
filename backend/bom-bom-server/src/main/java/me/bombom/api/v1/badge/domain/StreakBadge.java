package me.bombom.api.v1.badge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@DiscriminatorValue("STREAK")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StreakBadge extends Badge {

    @Column(name = "streak_day_count", columnDefinition = "SMALLINT")
    private int streakDayCount;

    @Builder
    public StreakBadge(
            Long id,
            @NonNull Long memberId,
            int streakDayCount
    ) {
        super(id, memberId, BadgeCategory.STREAK);
        this.streakDayCount = streakDayCount;
    }
}
