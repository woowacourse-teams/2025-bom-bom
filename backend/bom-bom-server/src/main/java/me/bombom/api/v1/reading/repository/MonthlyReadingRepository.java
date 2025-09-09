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
		SET mr.rank_order = 
			    (SELECT r.rnk
				 FROM (SELECT m.member_id, RANK() OVER (ORDER BY m.current_count DESC) AS rnk
				 FROM monthly_reading m) r
			  	 WHERE r.member_id = mr.member_id),
			mr.next_rank_difference = 
				COALESCE((SELECT MIN(x.current_count)
						  FROM monthly_reading x
						  WHERE x.current_count > mr.current_count) -  mr.current_count, 0);
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
