package me.bombom.api.v1.member.domain;

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
public class TodayReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int totalCount;

    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int currentCount;

    @Builder
    public TodayReading(
            Long id,
            @NonNull Long memberId,
            int totalCount,
            int currentCount
    ) {
        this.id = id;
        this.memberId = memberId;
        this.totalCount = totalCount;
        this.currentCount = currentCount;
    }
}
