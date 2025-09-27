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
		FROM monthly_reading mr
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
  			  member_id,
  			  RANK() OVER (ORDER BY current_count DESC) AS calculated_rank,
              COALESCE(
	            LEAD(current_count) OVER (ORDER BY current_count DESC) - current_count,
                0
	  		  ) AS next_diff
  		  FROM monthly_reading_realtime
  		) ranks ON mrs.member_id = ranks.member_id
  		SET
  		  mrs.rank_order = ranks.calculated_rank,
  		  mrs.next_rank_difference = diffs.next_diff;
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

	void deleteByMemberId(Long memberId);
}
