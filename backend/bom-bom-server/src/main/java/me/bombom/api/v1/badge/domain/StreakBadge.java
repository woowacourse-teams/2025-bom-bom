package me.bombom.api.v1.badge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "streak_badge_tier", length = 20)
    private StreakBadgeTier streakBadgeTier;

    @Builder
    public StreakBadge(
            Long id,
            @NonNull Long memberId,
            @NonNull StreakBadgeTier streakBadgeTier
    ) {
        super(id, memberId, BadgeCategory.STREAK);
        this.streakBadgeTier = streakBadgeTier;
    }
}
