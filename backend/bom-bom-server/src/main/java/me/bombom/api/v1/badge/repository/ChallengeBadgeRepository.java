package me.bombom.api.v1.badge.repository;

import me.bombom.api.v1.badge.domain.ChallengeBadge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeBadgeRepository extends JpaRepository<ChallengeBadge, Long> {

    boolean existsByMemberIdAndChallengeId(Long memberId, Long challengeId);
}
