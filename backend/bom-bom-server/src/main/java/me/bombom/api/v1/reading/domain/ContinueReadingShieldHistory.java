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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_continue_reading_shield_history_member_type_reason_date",
                columnNames = { "member_id", "type", "reason", "event_date" }
        )
})
public class ContinueReadingShieldHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContinueReadingShieldHistoryType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContinueReadingShieldHistoryReason reason;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int quantity;

    @Builder
    public ContinueReadingShieldHistory(
            Long id,
            @NonNull Long memberId,
            @NonNull ContinueReadingShieldHistoryType type,
            @NonNull ContinueReadingShieldHistoryReason reason,
            @NonNull LocalDate eventDate,
            int quantity
    ) {
        this.id = id;
        this.memberId = memberId;
        this.type = type;
        this.reason = reason;
        this.eventDate = eventDate;
        this.quantity = quantity;
    }

    public static ContinueReadingShieldHistory grant(
            Long memberId,
            ContinueReadingShieldHistoryReason reason,
            LocalDate eventDate,
            int quantity
    ) {
        return ContinueReadingShieldHistory.builder()
                .memberId(memberId)
                .type(ContinueReadingShieldHistoryType.GRANT)
                .reason(reason)
                .eventDate(eventDate)
                .quantity(quantity)
                .build();
    }

    public static ContinueReadingShieldHistory use(
            Long memberId,
            ContinueReadingShieldHistoryReason reason,
            LocalDate eventDate,
            int quantity
    ) {
        return ContinueReadingShieldHistory.builder()
                .memberId(memberId)
                .type(ContinueReadingShieldHistoryType.USE)
                .reason(reason)
                .eventDate(eventDate)
                .quantity(quantity)
                .build();
    }
}
