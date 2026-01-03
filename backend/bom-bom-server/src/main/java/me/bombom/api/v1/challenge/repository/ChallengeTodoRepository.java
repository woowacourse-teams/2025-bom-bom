package me.bombom.api.v1.challenge.repository;

import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeTodoRepository extends JpaRepository<ChallengeTodo, Long> {

    Optional<ChallengeTodo> findByChallengeIdAndTodoType(Long challengeId, ChallengeTodoType todoType);
}
