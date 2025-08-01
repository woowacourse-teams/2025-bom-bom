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
public class TodayReading extends BaseEntity {

    private static final int INITIAL_TOTAL_COUNT = 0;
    private static final int INITIAL_CURRENT_COUNT = 0;
    private static final int RESET_TOTAL_COUNT = 0;
    private static final int RESET_CURRENT_COUNT = 0;
    private static final int INCREASE_CURRENT_COUNT = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int totalCount;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int currentCount;

    @Builder
    public TodayReading(
            Long id,
            @NonNull Long memberId,
            int totalCount,
            int currentCount
    ) {
        this.id = id;
        this.memberId = memberId;
        this.totalCount = totalCount;
        this.currentCount = currentCount;
    }

    public static TodayReading create(Long memberId) {
        return TodayReading.builder()
                .memberId(memberId)
                .totalCount(INITIAL_TOTAL_COUNT)
                .currentCount(INITIAL_CURRENT_COUNT)
                .build();
    }

    public void resetCount() {
        totalCount = RESET_TOTAL_COUNT;
        currentCount = RESET_CURRENT_COUNT;
    }

    public void increaseCurrentCount() {
        this.currentCount += INCREASE_CURRENT_COUNT;
    }
}
