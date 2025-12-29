package me.bombom.api.v1.challenge.repository;

import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, Long> {

    Optional<ChallengeParticipant> findByChallengeIdAndMemberId(Long challengeId, Long memberId);
}
