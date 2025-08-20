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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "unique_member_id_year",
                columnNames = {"member_id", "readingYear"}
        )
)
public class YearlyReading {

    private static final int INITIAL_CURRENT_COUNT = 0;
    private static final int RESET_CURRENT_COUNT = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private int currentCount;

    @Column(nullable = false)
    private int readingYear;

    @Builder
    public YearlyReading(
            Long id,
            @NonNull Long memberId,
            int currentCount,
            int readingYear
    ) {
        this.id = id;
        this.memberId = memberId;
        this.currentCount = currentCount;
        this.readingYear = readingYear;
    }

    public static YearlyReading create(Long memberId, int year) {
        return YearlyReading.builder()
                .memberId(memberId)
                .readingYear(year)
                .currentCount(INITIAL_CURRENT_COUNT)
                .build();
    }

    public void resetCurrentCount() {
        this.currentCount = RESET_CURRENT_COUNT;
    }

    public void increaseCurrentCount(int count) {
        this.currentCount += count;
    }
}

