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
public class MonthlyReadingSnapshot extends BaseEntity {

    private static final int INITIAL_CURRENT_COUNT = 0;
    private static final int RESET_CURRENT_COUNT = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private int currentCount;

    @Column(nullable = false)
    private long rankOrder;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private long nextRankDifference;

    @Builder
    public MonthlyReadingSnapshot(
            Long id,
            @NonNull Long memberId,
            int currentCount,
            long rankOrder,
            long nextRankDifference
    ) {
        this.id = id;
        this.memberId = memberId;
        this.currentCount = currentCount;
        this.rankOrder = rankOrder;
        this.nextRankDifference = nextRankDifference;
    }

    public static MonthlyReadingSnapshot create(Long memberId, long lowestRank, long lowestDifference) {
        return MonthlyReadingSnapshot.builder()
                .memberId(memberId)
                .currentCount(INITIAL_CURRENT_COUNT)
                .rankOrder(lowestRank)
                .nextRankDifference(lowestDifference)
                .build();
    }

    public void resetCurrentCount() {
        this.currentCount = RESET_CURRENT_COUNT;
    }
}
