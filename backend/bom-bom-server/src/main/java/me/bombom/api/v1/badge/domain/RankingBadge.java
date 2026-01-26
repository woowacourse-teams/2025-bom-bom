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
@DiscriminatorValue("RANKING")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingBadge extends Badge {

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_grade", length = 20)
    private BadgeGrade grade;

    private Integer periodYear;

    private Integer periodMonth;

    @Builder
    public RankingBadge(
            Long id,
            @NonNull Long memberId,
            @NonNull BadgeGrade grade,
            @NonNull Integer periodYear,
            @NonNull Integer periodMonth
    ) {
        super(id, memberId, BadgeCategory.RANKING);
        this.grade = grade;
        this.periodYear = periodYear;
        this.periodMonth = periodMonth;
    }
}
