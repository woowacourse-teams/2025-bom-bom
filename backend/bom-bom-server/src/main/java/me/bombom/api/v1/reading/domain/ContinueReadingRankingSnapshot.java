package me.bombom.api.v1.reading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "continue_reading_ranking_snapshot")
public class ContinueReadingRankingSnapshot {

    @Id
    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private int dayCount;

    @Column(nullable = false)
    private long rankOrder;

    public ContinueReadingRankingSnapshot(
            Long memberId,
            int dayCount,
            long rankOrder
    ) {
        this.memberId = memberId;
        this.dayCount = dayCount;
        this.rankOrder = rankOrder;
    }
}
