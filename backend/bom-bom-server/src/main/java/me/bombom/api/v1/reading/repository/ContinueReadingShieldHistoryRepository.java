package me.bombom.api.v1.reading.repository;

import java.time.LocalDate;
import me.bombom.api.v1.reading.domain.ContinueReadingShieldHistory;
import me.bombom.api.v1.reading.domain.ContinueReadingShieldHistoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContinueReadingShieldHistoryRepository extends JpaRepository<ContinueReadingShieldHistory, Long> {

    boolean existsByMemberIdAndTypeAndEventDate(
            Long memberId,
            ContinueReadingShieldHistoryType type,
            LocalDate eventDate
    );

    long countByMemberIdAndTypeAndEventDate(
            Long memberId,
            ContinueReadingShieldHistoryType type,
            LocalDate eventDate
    );

    @Modifying
    @Query(value = """
        INSERT IGNORE INTO continue_reading_shield_history (
            member_id,
            type,
            event_date,
            quantity,
            created_at,
            updated_at
        )
        SELECT
            shield.member_id,
            'GRANT',
            :eventDate,
            :quantity,
            NOW(6),
            NOW(6)
        FROM continue_reading_shield shield
        LEFT JOIN continue_reading_shield_history history
            ON history.member_id = shield.member_id
            AND history.type = 'GRANT'
            AND history.event_date = :eventDate
        WHERE history.id IS NULL
    """, nativeQuery = true)
    int insertMonthlyGrantHistories(
            @Param("eventDate") LocalDate eventDate,
            @Param("quantity") int quantity
    );

    void deleteByMemberId(Long memberId);
}
