package me.bombom.api.v1.badge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;

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
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                        .addContext("streakDayCount", streakDayCount));
    }
}
