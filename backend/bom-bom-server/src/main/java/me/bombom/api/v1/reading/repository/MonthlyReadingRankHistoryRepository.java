package me.bombom.api.v1.reading.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.MonthlyReadingRankHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonthlyReadingRankHistoryRepository extends JpaRepository<MonthlyReadingRankHistory, Long> {

    Optional<MonthlyReadingRankHistory> findByMemberIdAndPeriod(
            Long memberId,
            LocalDate period
    );

    @Query("""
            SELECT h
            FROM MonthlyReadingRankHistory h
            WHERE h.memberId = :memberId
              AND h.period < :period
            ORDER BY h.period DESC
            """)
    List<MonthlyReadingRankHistory> findRecentBeforePeriodByMemberId(
            @Param("memberId") Long memberId,
            @Param("period") LocalDate period,
            Pageable pageable
    );

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO monthly_reading_rank_history (
                member_id,
                period,
                read_count,
                rank_order,
                created_at,
                updated_at
            )
            SELECT
                r.member_id,
                :period,
                r.current_count,
                RANK() OVER (ORDER BY r.current_count DESC),
                NOW(6),
                NOW(6)
            FROM monthly_reading_realtime r
            """, nativeQuery = true)
    void saveCurrentMonthlyRanking(
            @Param("period") LocalDate period
    );
}
