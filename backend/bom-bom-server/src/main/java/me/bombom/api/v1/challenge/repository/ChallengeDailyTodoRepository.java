package me.bombom.api.v1.challenge.repository;

import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeDailyTodoRepository extends JpaRepository<ChallengeDailyTodo, Long> {

    boolean existsByParticipantIdAndTodoDateAndChallengeTodoId(Long participantId, LocalDate todoDate, Long challengeTodoId);
}
