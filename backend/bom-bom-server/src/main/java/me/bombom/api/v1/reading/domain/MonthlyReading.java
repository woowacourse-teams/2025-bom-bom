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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyReading {

    private static final int INITIAL_CURRENT_COUNT = 0;
    private static final int RESET_CURRENT_COUNT = 0;
    private static final int INCREASE_CURRENT_COUNT = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private int currentCount;

    @Builder
    public MonthlyReading(
            Long id,
            @NonNull Long memberId,
            int currentCount
    ) {
        this.id = id;
        this.memberId = memberId;
        this.currentCount = currentCount;
    }

    public static MonthlyReading create(Long memberId) {
        return MonthlyReading.builder()
                .memberId(memberId)
                .currentCount(INITIAL_CURRENT_COUNT)
                .build();
    }

    public void resetCurrentCount() {
        this.currentCount = RESET_CURRENT_COUNT;
    }

    public void increaseCurrentCount() {
        this.currentCount += INCREASE_CURRENT_COUNT;
    }
}
