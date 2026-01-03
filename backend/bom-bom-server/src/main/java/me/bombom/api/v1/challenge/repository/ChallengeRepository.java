package me.bombom.api.v1.challenge.repository;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.challenge.domain.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    @Query("""
        SELECT c
        FROM Challenge c
        WHERE :date BETWEEN c.startDate AND c.endDate
    """)
    List<Challenge> findOngoingChallenges(@Param("date") LocalDate date);
}
