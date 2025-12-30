package me.bombom.api.v1.challenge.service;

import me.bombom.api.v1.challenge.domain.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
}
