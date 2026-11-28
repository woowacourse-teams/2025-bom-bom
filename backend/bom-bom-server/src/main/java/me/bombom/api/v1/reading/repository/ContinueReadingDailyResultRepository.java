package me.bombom.api.v1.reading.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.ContinueReadingDailyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContinueReadingDailyResultRepository extends JpaRepository<ContinueReadingDailyResult, Long> {

    List<ContinueReadingDailyResult> findAllByMemberIdAndResultDateBetween(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
        SELECT MIN(r.resultDate)
        FROM ContinueReadingDailyResult r
    """)
    Optional<LocalDate> findFirstResultDate();

    @Modifying
    @Query(value = """
        INSERT IGNORE INTO continue_reading_daily_result (member_id, result_date, result)
        SELECT tr.member_id, :resultDate, 'READ'
        FROM today_reading tr
        WHERE tr.read_count > 0
    """, nativeQuery = true)
    int bulkInsertReadResults(@Param("resultDate") LocalDate resultDate);

    @Modifying
    @Query(value = """
        INSERT IGNORE INTO continue_reading_daily_result (member_id, result_date, result)
        SELECT tr.member_id, :resultDate, 'NO_ARTICLE'
        FROM today_reading tr
        WHERE tr.total_count = 0
          AND tr.read_count = 0
    """, nativeQuery = true)
    int bulkInsertNoArticleResults(@Param("resultDate") LocalDate resultDate);

    @Modifying
    @Query("""
        DELETE FROM ContinueReadingDailyResult r
        WHERE r.resultDate < :threshold
    """)
    int bulkDeleteBefore(@Param("threshold") LocalDate threshold);

    @Modifying
    @Query("""
        DELETE FROM ContinueReadingDailyResult r
        WHERE r.memberId = :memberId
    """)
    void bulkDeleteByMemberId(@Param("memberId") Long memberId);
}
