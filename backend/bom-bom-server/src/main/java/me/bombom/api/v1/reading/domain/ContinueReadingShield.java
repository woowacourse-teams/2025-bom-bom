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
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_continue_reading_shield_member_id", columnNames = "member_id")
})
public class ContinueReadingShield extends BaseEntity {

    private static final int INITIAL_REMAINING_COUNT = 1;
    private static final int USE_COUNT = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "SMALLINT")
    private int remainingCount;

    @Builder
    public ContinueReadingShield(
            Long id,
            @NonNull Long memberId,
            int remainingCount
    ) {
        this.id = id;
        this.memberId = memberId;
        this.remainingCount = Math.max(remainingCount, 0);
    }

    public static ContinueReadingShield create(Long memberId) {
        return ContinueReadingShield.builder()
                .memberId(memberId)
                .remainingCount(INITIAL_REMAINING_COUNT)
                .build();
    }

    public boolean useIfAvailable() {
        if (remainingCount <= 0) {
            return false;
        }
        remainingCount -= USE_COUNT;
        return true;
    }
}
