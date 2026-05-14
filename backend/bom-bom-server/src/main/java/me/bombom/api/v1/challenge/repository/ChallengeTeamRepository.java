package me.bombom.api.v1.challenge.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeTeamRepository extends JpaRepository<ChallengeTeam, Long> {

    @Query("""
        SELECT ct
        FROM ChallengeTeam ct
        WHERE ct.challengeId = :challengeId
        ORDER BY ct.id
    """)
    List<ChallengeTeam> findAllByChallengeIdOrderById(@Param("challengeId") Long challengeId);

    @Query("""
        SELECT ct
        FROM ChallengeTeam ct
        LEFT JOIN ChallengeParticipant cp ON cp.challengeTeamId = ct.id
        WHERE ct.challengeId = :challengeId
        GROUP BY ct
        ORDER BY COUNT(cp.id), ct.id
        LIMIT 1
    """)
    Optional<ChallengeTeam> findTeamWithFewestMembers(@Param("challengeId") Long challengeId);

    @Modifying
    @Query("""
        UPDATE ChallengeTeam ct
        SET ct.progress = 0
        WHERE ct.challengeId IN :challengeIds
    """)
    void resetProgressByChallengeIdIn(@Param("challengeIds") List<Long> challengeIds);
}
