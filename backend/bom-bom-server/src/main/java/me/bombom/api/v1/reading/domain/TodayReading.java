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

    private static final int INITIAL_TOTAL_COUNT = 3;
    private static final int INITIAL_CURRENT_COUNT = 0;
    private static final int INITIAL_READ_COUNT = 0;
    private static final int RESET_TOTAL_COUNT = 0;
    private static final int RESET_CURRENT_COUNT = 0;
    private static final int RESET_READ_COUNT = 0;
    private static final int INCREASE_CURRENT_COUNT = 1;
    private static final int INCREASE_READ_COUNT = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    // 오늘 도착한 뉴스레터 개수
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int totalCount;

    // 오늘 도착한 뉴스레터 중 읽은 개수
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int currentCount;

    // 안 읽은 뉴스레터 중 읽은 개수 (도착일 무관)
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int readCount;

    @Builder
    public TodayReading(
        Long id,
        @NonNull Long memberId,
        int totalCount,
        int currentCount,
        int readCount
    ) {
        this.id = id;
        this.memberId = memberId;
        this.totalCount = totalCount;
        this.currentCount = currentCount;
        this.readCount = readCount;
    }

    public static TodayReading create(Long memberId) {
        return TodayReading.builder()
            .memberId(memberId)
            .totalCount(INITIAL_TOTAL_COUNT)
            .currentCount(INITIAL_CURRENT_COUNT)
            .readCount(INITIAL_READ_COUNT)
            .build();
    }

    public void resetCount() {
        this.totalCount = RESET_TOTAL_COUNT;
        this.currentCount = RESET_CURRENT_COUNT;
        this.readCount = RESET_READ_COUNT;
    }

    public void increaseCurrentCount() {
        this.currentCount += INCREASE_CURRENT_COUNT;
    }

    public void increaseReadCount() {
        this.readCount += INCREASE_READ_COUNT;
    }
}
