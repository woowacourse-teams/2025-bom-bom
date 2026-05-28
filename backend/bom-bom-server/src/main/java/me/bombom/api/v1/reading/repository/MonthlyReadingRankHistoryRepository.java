package me.bombom.api.v1.reading.repository;

import java.util.Optional;
import me.bombom.api.v1.reading.domain.MonthlyReadingRankHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonthlyReadingRankHistoryRepository extends JpaRepository<MonthlyReadingRankHistory, Long> {

    Optional<MonthlyReadingRankHistory> findByMemberIdAndPeriodYearAndPeriodMonth(
            Long memberId,
            int periodYear,
            int periodMonth
    );

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO monthly_reading_rank_history (
                member_id,
                period_year,
                period_month,
                read_count,
                rank_order,
                created_at,
                updated_at
            )
            SELECT
                r.member_id,
                :periodYear,
                :periodMonth,
                r.current_count,
                RANK() OVER (ORDER BY r.current_count DESC),
                NOW(6),
                NOW(6)
            FROM monthly_reading_realtime r
            """, nativeQuery = true)
    void saveCurrentMonthlyRanking(
            @Param("periodYear") int periodYear,
            @Param("periodMonth") int periodMonth
    );
}
