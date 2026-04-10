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
public class ContinueReadingSnapshot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private int dayCount;

    @Column(nullable = false)
    private long rankOrder;

    @Builder
    public ContinueReadingSnapshot(
            Long id,
            @NonNull Long memberId,
            int dayCount,
            long rankOrder
    ) {
        this.id = id;
        this.memberId = memberId;
        this.dayCount = dayCount;
        this.rankOrder = rankOrder;
    }

    public static ContinueReadingSnapshot create(Long memberId, int dayCount, long rankOrder) {
        return ContinueReadingSnapshot.builder()
                .memberId(memberId)
                .dayCount(dayCount)
                .rankOrder(rankOrder)
                .build();
    }
}
