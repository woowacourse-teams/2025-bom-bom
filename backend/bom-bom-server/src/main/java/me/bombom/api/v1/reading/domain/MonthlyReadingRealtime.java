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
public class MonthlyReadingRealtime extends BaseEntity {

    private static final int INITIAL_CURRENT_COUNT = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private int currentCount;

    @Builder
    public MonthlyReadingRealtime(
            Long id,
            @NonNull Long memberId,
            int currentCount
    ) {
        this.id = id;
        this.memberId = memberId;
        this.currentCount = currentCount;
    }

    public static MonthlyReadingRealtime create(Long memberId) {
        return MonthlyReadingRealtime.builder()
                .memberId(memberId)
                .currentCount(INITIAL_CURRENT_COUNT)
                .build();
    }

    public void increaseCurrentCount() {
        this.currentCount++;
    }
}
