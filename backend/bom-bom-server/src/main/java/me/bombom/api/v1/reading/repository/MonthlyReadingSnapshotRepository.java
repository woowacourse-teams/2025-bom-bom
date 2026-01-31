package me.bombom.api.v1.reading.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.MonthlyReadingSnapshot;
import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;
import me.bombom.api.v1.reading.dto.RankerInfo;
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
			mr.current_count AS monthlyReadCount,
			mr.next_rank_difference AS nextRankDifference,
			rb.badge_grade AS rankingBadgeGrade,
			rb.period_year AS rankingBadgeYear,
			rb.period_month AS rankingBadgeMonth,
			cb_latest.badge_grade AS challengeBadgeGrade,
			cb_latest.challenge_name AS challengeBadgeName,
			cb_latest.challenge_generation AS challengeBadgeGeneration
		FROM monthly_reading_snapshot mr
		JOIN member m ON mr.member_id = m.id
		LEFT JOIN badge rb ON rb.member_id = mr.member_id
			AND rb.badge_category = 'RANKING'
			AND rb.period_year = :lastMonthYear
			AND rb.period_month = :lastMonthValue
		LEFT JOIN LATERAL (
			SELECT cb.*
			FROM badge cb
			WHERE cb.member_id = mr.member_id
				AND cb.badge_category = 'CHALLENGE'
			ORDER BY cb.created_at DESC
			LIMIT 1
		) cb_latest ON true
		WHERE mr.rank_order IS NOT NULL
		ORDER BY mr.rank_order ASC, m.nickname ASC
		LIMIT :limit
	""", nativeQuery = true)
	List<MonthlyReadingRankFlat> findMonthlyRanking(
			@Param("limit") int limit,
			@Param("lastMonthYear") Integer lastMonthYear,
			@Param("lastMonthValue") Integer lastMonthValue
	);

	@Query(value = """
		SELECT mr.member_id AS memberId, mr.rank_order AS rankOrder
		FROM monthly_reading_snapshot mr
		WHERE mr.rank_order IS NOT NULL
			AND mr.rank_order <= :maxRank
		ORDER BY mr.rank_order ASC
	""", nativeQuery = true)
	List<RankerInfo> findTopRankers(@Param("maxRank") long maxRank);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
		UPDATE monthly_reading_snapshot mrs
		JOIN (
			SELECT
				r.member_id,
				RANK() OVER (ORDER BY r.current_count DESC) AS calculated_rank,
				COALESCE(ds.prev_distinct - r.current_count, 0) AS next_diff,
				r.current_count AS current_count
			FROM monthly_reading_realtime r
			JOIN (
				SELECT
					t.current_count AS cnt,
					LAG(t.current_count) OVER (ORDER BY t.current_count DESC) AS prev_distinct
				FROM (SELECT DISTINCT current_count FROM monthly_reading_realtime) t
			) ds ON ds.cnt = r.current_count
		) ranks ON mrs.member_id = ranks.member_id
		SET
			mrs.current_count = ranks.current_count,
			mrs.rank_order = ranks.calculated_rank,
			mrs.next_rank_difference = ranks.next_diff
	""", nativeQuery = true)
	void updateMonthlyRanking();

	@Query(value = """
		SELECT
			m.nickname AS nickname,
			mr.rank_order AS `rank`,
			mr.current_count AS monthlyReadCount,
			mr.next_rank_difference AS nextRankDifference,
			rb.badge_grade AS rankingBadgeGrade,
			rb.period_year AS rankingBadgeYear,
			rb.period_month AS rankingBadgeMonth,
			cb_latest.badge_grade AS challengeBadgeGrade,
			cb_latest.challenge_name AS challengeBadgeName,
			cb_latest.challenge_generation AS challengeBadgeGeneration
		FROM monthly_reading_snapshot mr
		JOIN member m ON mr.member_id = m.id
		LEFT JOIN badge rb ON rb.member_id = mr.member_id
			AND rb.badge_category = 'RANKING'
			AND rb.period_year = :lastMonthYear
			AND rb.period_month = :lastMonthValue
		LEFT JOIN LATERAL (
			SELECT cb.*
			FROM badge cb
			WHERE cb.member_id = mr.member_id
				AND cb.badge_category = 'CHALLENGE'
			ORDER BY cb.created_at DESC
			LIMIT 1
		) cb_latest ON true
		WHERE mr.member_id = :memberId
			AND mr.rank_order IS NOT NULL
	""", nativeQuery = true)
	MonthlyReadingRankFlat findMemberRanking(
			@Param("memberId") Long memberId,
			@Param("lastMonthYear") Integer lastMonthYear,
			@Param("lastMonthValue") Integer lastMonthValue
	);

	MonthlyReadingSnapshot findTopByOrderByRankOrderDesc();

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE MonthlyReadingSnapshot mrs SET mrs.currentCount = 0")
	void resetAllCurrentCount();

	void deleteByMemberId(Long memberId);
}
