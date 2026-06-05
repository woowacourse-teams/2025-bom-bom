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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE continue_reading_shield shield
        SET shield.remaining_count = shield.remaining_count - :quantity
        WHERE shield.member_id = :memberId
            AND shield.remaining_count >= :quantity
            AND NOT EXISTS (
                SELECT 1
                FROM continue_reading_shield_history history
                WHERE history.member_id = :memberId
                    AND history.type = 'USE'
                    AND history.event_date = :eventDate
            )
    """, nativeQuery = true)
    int useIfAvailable(
            @Param("memberId") Long memberId,
            @Param("eventDate") LocalDate eventDate,
            @Param("quantity") int quantity
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE continue_reading_shield shield
        LEFT JOIN continue_reading_shield_history history
            ON history.member_id = shield.member_id
            AND history.type = 'GRANT'
            AND history.event_date = :eventDate
        SET shield.remaining_count = :remainingCount
        WHERE history.id IS NULL
    """, nativeQuery = true)
    int resetMonthlyIfNotGranted(
            @Param("eventDate") LocalDate eventDate,
            @Param("remainingCount") int remainingCount
    );

    void deleteByMemberId(Long memberId);
}
