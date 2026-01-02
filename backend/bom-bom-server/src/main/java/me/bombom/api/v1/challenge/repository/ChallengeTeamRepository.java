package me.bombom.api.v1.challenge.repository;

import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeTeamRepository extends JpaRepository<ChallengeTeam, Long> {
}
