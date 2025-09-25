package me.bombom.api.v1.reading.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.MonthlyReadingSnapshot;
import me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.MonthlyReadingRankResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonthlyReadingSnapshotRepository extends JpaRepository<MonthlyReadingSnapshot, Long> {

	Optional<MonthlyReadingSnapshot> findByMemberId(Long memberId);

	@Query(value = """
		SELECT
			m.nickname AS nickname,
			mr.rank_order AS `rank`,
			mr.current_count AS monthlyReadCount
		FROM monthly_reading_snapshot mr
		JOIN member m ON mr.member_id = m.id
		WHERE mr.rank_order IS NOT NULL
		ORDER BY mr.rank_order ASC, m.nickname ASC
		LIMIT :limit
	""", nativeQuery = true)
	List<MonthlyReadingRankResponse> findMonthlyRanking(@Param("limit") int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
		UPDATE monthly_reading_snapshot mrs
        JOIN (
            SELECT
              r.member_id,
              RANK() OVER (ORDER BY r.current_count DESC) AS calculated_rank,
              /* 다음(위 등수) distinct 점수 - 내 점수 */
              COALESCE(ds.prev_distinct - r.current_count, 0) AS next_diff
            FROM monthly_reading_realtime r
            JOIN (
              SELECT
                t.current_count AS cnt,
                LAG(t.current_count) OVER (ORDER BY t.current_count DESC) AS prev_distinct
              FROM (SELECT DISTINCT current_count FROM monthly_reading_realtime) t
            ) ds ON ds.cnt = r.current_count
        ) ranks ON mrs.member_id = ranks.member_id
        SET
            mrs.rank_order = ranks.calculated_rank,
            mrs.next_rank_difference = ranks.next_diff
	""", nativeQuery = true)
    void updateMonthlyRanking();

	@Query("""
		SELECT new me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingRankResponse(
		  mr.rankOrder AS rank,
		  mr.currentCount AS readCount,
		  mr.nextRankDifference AS nextRankDifference
		)
		FROM MonthlyReadingSnapshot mr
		WHERE mr.memberId = :memberId
	""")
	MemberMonthlyReadingRankResponse findMemberRankAndGap(@Param("memberId") Long memberId);

	MonthlyReadingSnapshot findTopByOrderByRankOrderDesc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MonthlyReadingSnapshot mrs SET mrs.currentCount = 0")
    void resetAllCurrentCount();

	void deleteByMemberId(Long memberId);
}
