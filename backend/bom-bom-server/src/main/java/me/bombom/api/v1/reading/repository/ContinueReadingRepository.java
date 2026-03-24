package me.bombom.api.v1.reading.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.dto.ContinueReadingRankFlat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContinueReadingRepository extends JpaRepository<ContinueReading, Long> {

    Optional<ContinueReading> findByMemberId(Long memberId);

    @Query(value = """
            SELECT
                m.nickname AS nickname,
                ranks.calculated_rank AS `rank`,
                ranks.day_count AS dayCount,
                rb.badge_grade AS rankingBadgeGrade,
                rb.period_year AS rankingBadgeYear,
                rb.period_month AS rankingBadgeMonth,
                cb_latest.badge_grade AS challengeBadgeGrade,
                cb_latest.challenge_name AS challengeBadgeName,
                cb_latest.challenge_generation AS challengeBadgeGeneration
            FROM (
                SELECT
                    cr.member_id,
                    cr.day_count AS day_count,
                    RANK() OVER (ORDER BY cr.day_count DESC, cr.member_id ASC) AS calculated_rank
                FROM continue_reading cr
                WHERE cr.day_count > 0
            ) ranks
            JOIN member m ON ranks.member_id = m.id
            LEFT JOIN badge rb ON rb.member_id = ranks.member_id
                AND rb.badge_category = 'RANKING'
                AND rb.period_year = :lastMonthYear
                AND rb.period_month = :lastMonthValue
            LEFT JOIN LATERAL (
                SELECT cb.*
                FROM badge cb
                WHERE cb.member_id = ranks.member_id
                    AND cb.badge_category = 'CHALLENGE'
                ORDER BY cb.created_at DESC
                LIMIT 1
            ) cb_latest ON true
            ORDER BY ranks.calculated_rank ASC, m.nickname ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<ContinueReadingRankFlat> findContinueReadingRanking(
            @Param("limit") int limit,
            @Param("lastMonthYear") int lastMonthYear,
            @Param("lastMonthValue") int lastMonthValue
    );

    @Query(value = """
            SELECT
                m.nickname AS nickname,
                ranks.calculated_rank AS `rank`,
                ranks.day_count AS dayCount,
                rb.badge_grade AS rankingBadgeGrade,
                rb.period_year AS rankingBadgeYear,
                rb.period_month AS rankingBadgeMonth,
                cb_latest.badge_grade AS challengeBadgeGrade,
                cb_latest.challenge_name AS challengeBadgeName,
                cb_latest.challenge_generation AS challengeBadgeGeneration
            FROM (
                SELECT
                    cr.member_id,
                    cr.day_count AS day_count,
                    RANK() OVER (ORDER BY cr.day_count DESC, cr.member_id ASC) AS calculated_rank
                FROM continue_reading cr
            ) ranks
            JOIN member m ON ranks.member_id = m.id
            LEFT JOIN badge rb ON rb.member_id = ranks.member_id
                AND rb.badge_category = 'RANKING'
                AND rb.period_year = :lastMonthYear
                AND rb.period_month = :lastMonthValue
            LEFT JOIN LATERAL (
                SELECT cb.*
                FROM badge cb
                WHERE cb.member_id = ranks.member_id
                    AND cb.badge_category = 'CHALLENGE'
                ORDER BY cb.created_at DESC
                LIMIT 1
            ) cb_latest ON true
            WHERE ranks.member_id = :memberId
            """, nativeQuery = true)
    ContinueReadingRankFlat findMemberContinueReadingRanking(
            @Param("memberId") Long memberId,
            @Param("lastMonthYear") int lastMonthYear,
            @Param("lastMonthValue") int lastMonthValue
    );

    void deleteByMemberId(Long memberId);
}
