package me.bombom.api.v1.reading.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.ContinueReadingShield;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContinueReadingShieldRepository extends JpaRepository<ContinueReadingShield, Long> {

    Optional<ContinueReadingShield> findByMemberId(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT shield
        FROM ContinueReadingShield shield
        WHERE shield.memberId = :memberId
    """)
    Optional<ContinueReadingShield> findByMemberIdForUpdate(@Param("memberId") Long memberId);

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
