package me.bombom.api.v1.badge.domain;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeGrade {
    GOLD(1),
    SILVER(2),
    BRONZE(3),
    ;

    public static final int MAX_RANK_FOR_BADGE = 3;

    private final long rankOrder;

    public static Optional<BadgeGrade> fromRankOrder(long rankOrder) {
        return Arrays.stream(values())
                .filter(grade -> grade.rankOrder == rankOrder)
                .findFirst();
    }
}
