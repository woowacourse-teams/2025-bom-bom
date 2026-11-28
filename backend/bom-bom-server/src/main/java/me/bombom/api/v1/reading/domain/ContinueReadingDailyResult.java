package me.bombom.api.v1.reading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * 매일 0시 연속 읽기 초기화 때 판정한 전날의 결과.
 * 다른 테이블로 다시 추론할 수 없는 결과(READ, NO_ARTICLE)만 기록한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_continue_reading_daily_result_member_date",
                columnNames = { "member_id", "result_date" }
        )
})
public class ContinueReadingDailyResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private LocalDate resultDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContinueReadingDailyResultType result;

    @Builder
    public ContinueReadingDailyResult(
            Long id,
            @NonNull Long memberId,
            @NonNull LocalDate resultDate,
            @NonNull ContinueReadingDailyResultType result
    ) {
        this.id = id;
        this.memberId = memberId;
        this.resultDate = resultDate;
        this.result = result;
    }
}
