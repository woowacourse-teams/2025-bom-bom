package me.bombom.api.v1.reading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_continue_reading_rank_history_member_period",
                columnNames = {"member_id", "period"}
        )
)
public class ContinueReadingRankHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private LocalDate period;

    @Column(nullable = false)
    private int dayCount;

    @Column(nullable = false)
    private long rankOrder;

    @Builder
    public ContinueReadingRankHistory(
            Long id,
            @NonNull Long memberId,
            @NonNull LocalDate period,
            int dayCount,
            long rankOrder
    ) {
        this.id = id;
        this.memberId = memberId;
        this.period = period;
        this.dayCount = dayCount;
        this.rankOrder = rankOrder;
    }
}
