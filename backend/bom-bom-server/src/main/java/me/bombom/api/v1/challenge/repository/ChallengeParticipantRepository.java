package me.bombom.api.v1.challenge.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.ChallengeParticipantCount;
import me.bombom.api.v1.challenge.dto.MemberMedalParticipationFlat;
import me.bombom.api.v1.challenge.dto.MemberChallengeRankingStatsFlat;
import me.bombom.api.v1.challenge.dto.OngoingChallengeParticipantFlat;
import me.bombom.api.v1.challenge.dto.ChallengeProgressFlat;
import me.bombom.api.v1.challenge.dto.TeamChallengeProgressFlat;
import me.bombom.api.v1.challenge.dto.TeamTodayProgressCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, Long> {

    boolean existsByChallengeIdAndMemberId(Long challengeId, Long memberId);
    
    Optional<ChallengeParticipant> findByChallengeIdAndMemberId(Long challengeId, Long memberId);

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.TeamTodayProgressCount(
            COUNT(cp.id),
            COUNT(cdr.id)
        )
        FROM ChallengeParticipant cp
        LEFT JOIN ChallengeDailyResult cdr ON cdr.participantId = cp.id
            AND cdr.date = :today
            AND cdr.status = :status
        WHERE cp.challengeTeamId = :teamId
            AND cp.isSurvived = true
    """)
    TeamTodayProgressCount findTeamTodayProgressCount(
            @Param("teamId") Long teamId,
            @Param("today") LocalDate today,
            @Param("status") ChallengeDailyStatus status
    );

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.ChallengeParticipantCount(p.challengeId, COUNT(p.id))
        FROM ChallengeParticipant p
        WHERE p.challengeId IN :challengeIds
        GROUP BY p.challengeId
    """)
    List<ChallengeParticipantCount> countByChallengeIdInGroupByChallengeId(@Param("challengeIds") List<Long> challengeIds);

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.ChallengeProgressFlat(
            c.totalDays,
            cp.completedDays,
            cp.isSurvived,
            ct.todoType,
            CASE
                WHEN cdt.id IS NOT NULL THEN true
                ELSE false
            END,
            cp.streak,
            cp.shield
        )
        FROM ChallengeParticipant cp
        JOIN Challenge c ON cp.challengeId = c.id
        JOIN ChallengeTodo ct ON c.id = ct.challengeId
        LEFT JOIN ChallengeDailyTodo cdt ON cp.id = cdt.participantId
            AND ct.id = cdt.challengeTodoId
            AND cdt.todoDate = :today
        WHERE cp.challengeId = :challengeId AND cp.memberId = :memberId
        ORDER BY ct.todoType
    """)
    List<ChallengeProgressFlat> findMemberProgress(
            @Param("challengeId") Long challengeId,
            @Param("memberId") Long memberId,
            @Param("today") LocalDate today
    );

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.TeamChallengeProgressFlat(
            m.id,
            m.nickname,
            cp.isSurvived,
            cp.completedDays,
            c.totalDays,
            ct.progress,
            cdr.date,
            cdr.status
        )
        FROM ChallengeParticipant cp
        JOIN Member m ON cp.memberId = m.id
        JOIN Challenge c ON cp.challengeId = c.id
        JOIN ChallengeTeam ct ON cp.challengeTeamId = ct.id
        LEFT JOIN ChallengeDailyResult cdr ON cp.id = cdr.participantId
        WHERE cp.challengeTeamId = :teamId
        ORDER BY cp.completedDays DESC, m.id, cdr.date
    """)
    List<TeamChallengeProgressFlat> findTeamProgress(@Param("teamId") Long teamId);

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.MemberChallengeRankingStatsFlat(
            cp.memberId,
            COUNT(cp.id),
            SUM(CASE WHEN cp.isSurvived = true THEN 1L ELSE 0L END),
            AVG(CASE
                    WHEN c.totalDays <= 0 THEN 0.0
                    ELSE FLOOR(cp.completedDays * 100.0 / c.totalDays)
                END)
        )
        FROM ChallengeParticipant cp
        JOIN Challenge c ON cp.challengeId = c.id
        WHERE c.endDate < :today
        GROUP BY cp.memberId
    """)
    List<MemberChallengeRankingStatsFlat> findEndedChallengeAggregates(@Param("today") LocalDate today);

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.MemberMedalParticipationFlat(
            cp.memberId,
            cp.completedDays,
            c.totalDays,
            cp.isSurvived
        )
        FROM ChallengeParticipant cp
        JOIN Challenge c ON cp.challengeId = c.id
        WHERE c.endDate < :today
          AND cp.memberId = :memberId
    """)
    List<MemberMedalParticipationFlat> findEndedChallengeParticipationsByMemberId(
            @Param("memberId") Long memberId,
            @Param("today") LocalDate today
    );

    List<ChallengeParticipant> findByMemberIdAndChallengeIdIn(Long memberId, List<Long> challengeIds);

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.OngoingChallengeParticipantFlat(
            c.id,
            c.name,
            c.startDate,
            c.endDate,
            c.totalDays,
            cp.challengeTeamId,
            cp.memberId,
            cp.completedDays
        )
        FROM ChallengeParticipant cp
        JOIN Challenge c ON cp.challengeId = c.id
        WHERE c.id IN (
            SELECT cp2.challengeId
            FROM ChallengeParticipant cp2
            WHERE cp2.memberId = :memberId
        )
          AND c.startDate <= :today
          AND c.endDate >= :today
    """)
    List<OngoingChallengeParticipantFlat> findParticipantsOfMemberOngoingChallenges(
            @Param("memberId") Long memberId,
            @Param("today") LocalDate today
    );

    List<ChallengeParticipant> findAllByChallengeId(Long challengeId);

    @Query("""
        SELECT cp
        FROM ChallengeParticipant cp
        WHERE cp.challengeId = :challengeId
          AND cp.isSurvived = true
          AND NOT EXISTS (
              SELECT cdr
              FROM ChallengeDailyResult cdr
              WHERE cdr.participantId = cp.id
                AND cdr.date = :date
          )
    """)
    List<ChallengeParticipant> findAbsentees(@Param("challengeId") Long challengeId, @Param("date") LocalDate date);

    @Query("""
        SELECT cp
        FROM ChallengeParticipant cp
        JOIN ChallengeComment cc
        ON cc.participantId = cp.id
        WHERE cc.id = :commentId
    """)
    Optional<ChallengeParticipant> findAuthorByCommentId(@Param("commentId") Long commentId);

    @Query("""
        SELECT cp
        FROM ChallengeParticipant cp
        WHERE cp.challengeId = :challengeId
          AND cp.isSurvived = true
          AND NOT EXISTS (
              SELECT cdr
              FROM ChallengeDailyResult cdr
              WHERE cdr.participantId = cp.id
                AND cdr.date = :date
                AND cdr.status = 'COMPLETE'
          )
    """)
    List<ChallengeParticipant> findSurvivedParticipantsWithoutCompleteDailyResultByChallengeId(
            @Param("challengeId") Long challengeId,
            @Param("date") LocalDate date
    );
}
