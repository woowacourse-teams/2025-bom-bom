package me.bombom.api.v1.badge.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@DiscriminatorValue("RANKING")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingBadge extends Badge {

    private Integer periodYear;

    private Integer periodMonth;

    @Builder
    public RankingBadge(
            Long id,
            @NonNull Long memberId,
            @NonNull BadgeType badgeType,
            @NonNull Integer periodYear,
            @NonNull Integer periodMonth
    ) {
        super(id, memberId, badgeType);
        this.periodYear = periodYear;
        this.periodMonth = periodMonth;
    }

    public static RankingBadge create(Long memberId, BadgeType badgeType, LocalDate period) {
        return RankingBadge.builder()
                .memberId(memberId)
                .badgeType(badgeType)
                .periodYear(period.getYear())
                .periodMonth(period.getMonthValue())
                .build();
    }
}
