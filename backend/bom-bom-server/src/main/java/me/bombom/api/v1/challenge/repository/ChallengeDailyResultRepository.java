package me.bombom.api.v1.challenge.repository;

import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeDailyResultRepository extends JpaRepository<ChallengeDailyResult, Long> {
}
