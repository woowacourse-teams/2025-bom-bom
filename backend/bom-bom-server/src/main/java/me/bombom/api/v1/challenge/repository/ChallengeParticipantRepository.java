package me.bombom.api.v1.challenge.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.ChallengeParticipantCount;
import me.bombom.api.v1.challenge.dto.ChallengeProgressFlat;
import me.bombom.api.v1.challenge.dto.TeamChallengeProgressFlat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, Long> {

    long countByChallengeId(Long challengeId);

    Optional<ChallengeParticipant> findByChallengeIdAndMemberId(Long challengeId, Long memberId);

    boolean existsByChallengeIdAndMemberId(Long challengeId, Long memberId);

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.ChallengeParticipantCount(p.challengeId, COUNT(p.id))
        FROM ChallengeParticipant p
        WHERE p.challengeId IN :challengeIds
        GROUP BY p.challengeId
    """)
    List<ChallengeParticipantCount> countByChallengeIdInGroupByChallengeId(@Param("challengeIds") List<Long> challengeIds);

    List<ChallengeParticipant> findAllByMemberId(Long memberId);

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.ChallengeProgressFlat(
            c.totalDays,
            cp.completedDays,
            ct.todoType,
            CASE
                WHEN cdt.id IS NOT NULL THEN true
                ELSE false
            END
        )
        FROM ChallengeParticipant cp
        JOIN Challenge c ON cp.challengeId = c.id
        JOIN ChallengeTodo ct ON c.id = ct.challengeId
        LEFT JOIN ChallengeDailyTodo cdt ON cp.id = cdt.participantId
            AND ct.id = cdt.challengeTodoId
            AND cdt.todoDate = :today
        WHERE cp.challengeId = :challengeId AND cp.memberId = :memberId
        ORDER BY ct.todoType ASC
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
        ORDER BY m.id, cdr.date
    """)
    List<TeamChallengeProgressFlat> findTeamProgress(@Param("teamId") Long teamId);
}
