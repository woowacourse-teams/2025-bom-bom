package me.bombom.api.v1.reading.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.MonthlyReading;
import me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.MonthlyReadingRankResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonthlyReadingRepository extends JpaRepository<MonthlyReading, Long> {

	Optional<MonthlyReading> findByMemberId(Long memberId);

	@Query(value = """
		SELECT
			m.nickname AS nickname,
			mr.rank_order AS `rank`,
			mr.current_count AS monthlyReadCount
		FROM monthly_reading mr
		JOIN member m ON mr.member_id = m.id
		WHERE mr.rank_order IS NOT NULL
		ORDER BY mr.rank_order ASC, m.nickname ASC
		LIMIT :limit
	""", nativeQuery = true)
	List<MonthlyReadingRankResponse> findMonthlyRanking(@Param("limit") int limit);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
		UPDATE monthly_reading mr
  		INNER JOIN (
  		  SELECT
  			  member_id,
  			  RANK() OVER (ORDER BY current_count DESC) AS calculated_rank
  		  FROM monthly_reading
  		) ranks ON mr.member_id = ranks.member_id
  		LEFT JOIN (
  		  SELECT DISTINCT
  			  mr1.member_id,
  			  COALESCE(MIN(mr2.current_count) - mr1.current_count, 0) AS next_diff
  		  FROM monthly_reading mr1
  		  LEFT JOIN monthly_reading mr2 ON mr2.current_count > mr1.current_count
  		  GROUP BY mr1.member_id, mr1.current_count
  		) diffs ON mr.member_id = diffs.member_id
  		SET
  		  mr.rank_order = ranks.calculated_rank,
  		  mr.next_rank_difference = diffs.next_diff;
	""", nativeQuery = true)
	void updateMonthlyRanking();

	@Query("""
		SELECT new me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingRankResponse(
		  mr.rankOrder AS rank,
		  mr.currentCount AS readCount,
		  mr.nextRankDifference AS nextRankDifference
		)
		FROM MonthlyReading mr
		WHERE mr.memberId = :memberId
	""")
	MemberMonthlyReadingRankResponse findMemberRankAndGap(@Param("memberId") Long memberId);

	MonthlyReading findTopByOrderByRankOrderDesc();
}
