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
			mr.rank AS rank,
			mr.current_count AS monthlyReadCount
		FROM monthly_reading mr
		JOIN member m ON mr.member_id = m.id
		WHERE mr.rank IS NOT NULL
		ORDER BY mr.rank ASC, m.nickname ASC
		LIMIT :limit
	""", nativeQuery = true)
	List<MonthlyReadingRankResponse> findMonthlyRanking(@Param("limit") int limit);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
		UPDATE monthly_reading mr
		SET mr.rank = (
		    SELECT rnk FROM (
		      SELECT member_id,
		             DENSE_RANK() OVER (ORDER BY current_count DESC, member_id ASC) AS rnk
		      FROM monthly_reading
		    ) r
		    WHERE r.member_id = mr.member_id
		)
	""", nativeQuery = true)
	void updateMonthlyRanking();

	@Query("""
		SELECT new me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingRankResponse(
		  COALESCE(mr.rank, 0) AS rank,
		  mr.currentCount AS readCount,
		  COALESCE(ABS(mr.currentCount - prev.currentCount), 0) AS nextRankDifference
		)
		FROM MonthlyReading mr
		LEFT JOIN MonthlyReading prev ON prev.rank = mr.rank - 1
		WHERE mr.memberId = :memberId
	""")
	MemberMonthlyReadingRankResponse findMemberRankAndTotal(@Param("memberId") Long memberId);
}
