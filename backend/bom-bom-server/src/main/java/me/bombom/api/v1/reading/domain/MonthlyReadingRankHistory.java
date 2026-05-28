package me.bombom.api.v1.reading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
        name = "monthly_reading_rank_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_monthly_reading_rank_history_member_period",
                columnNames = {"member_id", "period_year", "period_month"}
        )
)
public class MonthlyReadingRankHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private int periodYear;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int periodMonth;

    @Column(nullable = false)
    private int readCount;

    @Column(nullable = false)
    private long rankOrder;

    @Builder
    public MonthlyReadingRankHistory(
            Long id,
            @NonNull Long memberId,
            int periodYear,
            int periodMonth,
            int readCount,
            long rankOrder
    ) {
        this.id = id;
        this.memberId = memberId;
        this.periodYear = periodYear;
        this.periodMonth = periodMonth;
        this.readCount = readCount;
        this.rankOrder = rankOrder;
    }
}
