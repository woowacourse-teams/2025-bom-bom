package me.bombom.api.v1.reading.repository;

import java.time.LocalDate;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.ContinueReadingRankHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContinueReadingRankHistoryRepository extends JpaRepository<ContinueReadingRankHistory, Long> {

    Optional<ContinueReadingRankHistory> findByMemberIdAndPeriod(
            Long memberId,
            LocalDate period
    );

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO continue_reading_rank_history (
                member_id,
                period,
                day_count,
                rank_order,
                created_at,
                updated_at
            )
            SELECT
                cr.member_id,
                :period,
                cr.day_count,
                RANK() OVER (ORDER BY cr.day_count DESC),
                NOW(6),
                NOW(6)
            FROM continue_reading_realtime cr
            """, nativeQuery = true)
    void saveCurrentContinueReadingRanking(
            @Param("period") LocalDate period
    );
}
