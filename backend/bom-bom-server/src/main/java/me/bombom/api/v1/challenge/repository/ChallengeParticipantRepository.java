package me.bombom.api.v1.challenge.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.ChallengeParticipantCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, Long> {

    boolean existsByChallengeIdAndMemberId(Long challengeId, Long memberId);

    Optional<ChallengeParticipant> findByChallengeIdAndMemberId(Long challengeId, Long memberId);

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.ChallengeParticipantCount(p.challengeId, COUNT(p.id))
        FROM ChallengeParticipant p
        WHERE p.challengeId IN :challengeIds
        GROUP BY p.challengeId
    """)
    List<ChallengeParticipantCount> countByChallengeIdInGroupByChallengeId(@Param("challengeIds") List<Long> challengeIds);
    
    List<ChallengeParticipant> findByMemberIdAndChallengeIdIn(Long memberId, List<Long> challengeIds);
}
