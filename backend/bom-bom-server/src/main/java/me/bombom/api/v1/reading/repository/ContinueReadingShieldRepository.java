package me.bombom.api.v1.reading.repository;

import java.time.LocalDate;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.ContinueReadingShield;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContinueReadingShieldRepository extends JpaRepository<ContinueReadingShield, Long> {

    Optional<ContinueReadingShield> findByMemberId(Long memberId);

    @Modifying
    @Query(value = """
        UPDATE continue_reading_shield shield
        SET shield.reward_remaining_count = CASE
                WHEN shield.monthly_remaining_count >= :deductCount THEN shield.reward_remaining_count
                ELSE shield.reward_remaining_count - (:deductCount - shield.monthly_remaining_count)
            END,
            shield.monthly_remaining_count = CASE
                WHEN shield.monthly_remaining_count >= :deductCount THEN shield.monthly_remaining_count - :deductCount
                ELSE 0
            END
        WHERE shield.member_id = :memberId
            AND shield.monthly_remaining_count + shield.reward_remaining_count >= :deductCount
            AND NOT EXISTS (
                SELECT 1
                FROM continue_reading_shield_history history
                WHERE history.member_id = :memberId
                    AND history.type = 'USE'
                    AND history.reason = :reason
                    AND history.event_date = :eventDate
            )
    """, nativeQuery = true)
    int bulkDecreaseRemainingCountIfUsable(
            @Param("memberId") Long memberId,
            @Param("reason") String reason,
            @Param("eventDate") LocalDate eventDate,
            @Param("deductCount") int deductCount
    );

    @Modifying
    @Query(value = """
        UPDATE continue_reading_shield shield
        LEFT JOIN continue_reading_shield_history history
            ON history.member_id = shield.member_id
            AND history.type = 'GRANT'
            AND history.reason = :reason
            AND history.event_date = :eventDate
        SET shield.monthly_remaining_count = :remainingCount
        WHERE history.id IS NULL
    """, nativeQuery = true)
    int bulkResetMonthlyIfNotGranted(
            @Param("reason") String reason,
            @Param("eventDate") LocalDate eventDate,
            @Param("remainingCount") int remainingCount
    );

    void deleteByMemberId(Long memberId);
}
