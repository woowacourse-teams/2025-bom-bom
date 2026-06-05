package me.bombom.api.v1.reading.domain;

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
public class ContinueReadingRealtime extends BaseEntity {

    private static final int INITIAL_DAY_COUNT = 0;
    private static final int RESET_DAY_COUNT = 0;
    private static final int INCREASE_DAY_COUNT = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private int dayCount;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private int maxDayCount;

    @Builder
    public ContinueReadingRealtime(
            Long id,
            @NonNull Long memberId,
            int dayCount,
            Integer maxDayCount
    ) {
        this.id = id;
        this.memberId = memberId;
        this.dayCount = dayCount;
        this.maxDayCount = maxDayCount != null ? Math.max(dayCount, maxDayCount) : dayCount;
    }

    public static ContinueReadingRealtime create(Long memberId) {
        return ContinueReadingRealtime.builder()
                .memberId(memberId)
                .dayCount(INITIAL_DAY_COUNT)
                .maxDayCount(INITIAL_DAY_COUNT)
                .build();
    }

    public void resetDayCount() {
        dayCount = RESET_DAY_COUNT;
    }

    public boolean hasActiveStreak() {
        return dayCount > RESET_DAY_COUNT;
    }

    public void increaseDayCount() {
        dayCount += INCREASE_DAY_COUNT;
        maxDayCount = Math.max(maxDayCount, dayCount);
    }
}
