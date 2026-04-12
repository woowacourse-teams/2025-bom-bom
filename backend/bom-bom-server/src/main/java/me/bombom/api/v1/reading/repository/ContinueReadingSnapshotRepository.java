package me.bombom.api.v1.reading.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.reading.domain.ContinueReadingSnapshot;
import me.bombom.api.v1.reading.dto.ContinueReadingRankFlat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContinueReadingSnapshotRepository extends JpaRepository<ContinueReadingSnapshot, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO continue_reading_snapshot (member_id, day_count, rank_order)
            SELECT
                r.member_id,
                r.day_count,
                r.calculated_rank
            FROM (
                SELECT
                    cr.member_id,
                    cr.day_count AS day_count,
                    -- 공동 순위가 필요하므로 day_count만 기준으로 rank를 계산합니다.
                    RANK() OVER (ORDER BY cr.day_count DESC) AS calculated_rank
                FROM continue_reading_realtime cr
            ) r
            ON DUPLICATE KEY UPDATE
                day_count = VALUES(day_count),
                rank_order = VALUES(rank_order)
            """, nativeQuery = true)
    void updateContinueReadingRankingSnapshot();

    ContinueReadingSnapshot findTopByOrderByRankOrderDesc();

    @Query(value = """
            SELECT
                m.nickname AS nickname,
                rs.rank_order AS `rank`,
                rs.day_count AS dayCount,
                rb.badge_grade AS rankingBadgeGrade,
                rb.period_year AS rankingBadgeYear,
                rb.period_month AS rankingBadgeMonth,
                cb_latest.badge_grade AS challengeBadgeGrade,
                cb_latest.challenge_name AS challengeBadgeName,
                cb_latest.challenge_generation AS challengeBadgeGeneration,
                sb_latest.streak_badge_tier AS streakBadgeTier
            FROM continue_reading_snapshot rs
            JOIN member m ON rs.member_id = m.id
            LEFT JOIN badge rb ON rb.member_id = rs.member_id
                AND rb.badge_category = 'RANKING'
                AND rb.period_year = :lastMonthYear
                AND rb.period_month = :lastMonthValue
            LEFT JOIN LATERAL (
                SELECT cb.*
                FROM badge cb
                WHERE cb.member_id = rs.member_id
                    AND cb.badge_category = 'CHALLENGE'
                ORDER BY cb.created_at DESC
                LIMIT 1
            ) cb_latest ON true
            LEFT JOIN LATERAL (
                SELECT sb.*
                FROM badge sb
                WHERE sb.member_id = rs.member_id
                    AND sb.badge_category = 'STREAK'
                ORDER BY sb.streak_day_count DESC,
                sb.created_at DESC
                LIMIT 1
            ) sb_latest ON true
            ORDER BY rs.rank_order, m.nickname
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
                rs.rank_order AS `rank`,
                rs.day_count AS dayCount,
                rb.badge_grade AS rankingBadgeGrade,
                rb.period_year AS rankingBadgeYear,
                rb.period_month AS rankingBadgeMonth,
                cb_latest.badge_grade AS challengeBadgeGrade,
                cb_latest.challenge_name AS challengeBadgeName,
                cb_latest.challenge_generation AS challengeBadgeGeneration,
                sb_latest.streak_badge_tier AS streakBadgeTier
            FROM continue_reading_snapshot rs
            JOIN member m ON rs.member_id = m.id
            LEFT JOIN badge rb ON rb.member_id = rs.member_id
                AND rb.badge_category = 'RANKING'
                AND rb.period_year = :lastMonthYear
                AND rb.period_month = :lastMonthValue
            LEFT JOIN LATERAL (
                SELECT cb.*
                FROM badge cb
                WHERE cb.member_id = rs.member_id
                    AND cb.badge_category = 'CHALLENGE'
                ORDER BY cb.created_at DESC
                LIMIT 1
            ) cb_latest ON true
            LEFT JOIN LATERAL (
                SELECT sb.*
                FROM badge sb
                WHERE sb.member_id = rs.member_id
                    AND sb.badge_category = 'STREAK'
                ORDER BY sb.streak_day_count DESC,
                sb.created_at DESC
                LIMIT 1
            ) sb_latest ON true
            WHERE rs.member_id = :memberId
            """, nativeQuery = true)
    Optional<ContinueReadingRankFlat> findMemberContinueReadingRanking(
            @Param("memberId") Long memberId,
            @Param("lastMonthYear") int lastMonthYear,
            @Param("lastMonthValue") int lastMonthValue
    );

    void deleteByMemberId(Long memberId);
}
