package me.bombom.api.v1.reading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContinueReadingSnapshot {

    @Id
    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private int dayCount;

    @Column(nullable = false)
    private long rankOrder;

    public ContinueReadingSnapshot(
            Long memberId,
            int dayCount,
            long rankOrder
    ) {
        this.memberId = memberId;
        this.dayCount = dayCount;
        this.rankOrder = rankOrder;
    }
}
