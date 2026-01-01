package me.bombom.api.v1.challenge.repository;

import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeDailyTodoRepository extends JpaRepository<ChallengeDailyTodo, Long> {
}
