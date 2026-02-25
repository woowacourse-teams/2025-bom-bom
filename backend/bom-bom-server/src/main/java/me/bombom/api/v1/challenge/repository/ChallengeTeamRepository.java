package me.bombom.api.v1.challenge.repository;

import java.util.List;
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
    List<ChallengeTeam> findAllByChallengeIdOrderByIdAsc(@Param("challengeId") Long challengeId);

    @Modifying
    @Query("""
        UPDATE ChallengeTeam ct
        SET ct.progress = 0
        WHERE ct.challengeId IN :challengeIds
    """)
    void resetProgressByChallengeIdIn(@Param("challengeIds") List<Long> challengeIds);
}
