package me.bombom.api.v1.reading.repository;

import java.time.LocalDate;
import me.bombom.api.v1.reading.domain.ContinueReadingShieldHistory;
import me.bombom.api.v1.reading.domain.ContinueReadingShieldHistoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContinueReadingShieldHistoryRepository extends JpaRepository<ContinueReadingShieldHistory, Long> {

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
            quantity
        )
        SELECT
            shield.member_id,
            'GRANT',
            :eventDate,
            :quantity
        FROM continue_reading_shield shield
    """, nativeQuery = true)
    int insertMonthlyGrantHistories(
            @Param("eventDate") LocalDate eventDate,
            @Param("quantity") int quantity
    );

    void deleteByMemberId(Long memberId);
}
