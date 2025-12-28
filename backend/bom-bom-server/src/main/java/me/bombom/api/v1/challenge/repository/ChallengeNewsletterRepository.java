package me.bombom.api.v1.challenge.repository;

import java.util.List;
import me.bombom.api.v1.challenge.domain.ChallengeNewsletter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeNewsletterRepository extends JpaRepository<ChallengeNewsletter, Long> {

    List<ChallengeNewsletter> findAllByChallengeId(Long challengeId);
    
    List<ChallengeNewsletter> findAllByChallengeIdIn(List<Long> challengeIds);
}
