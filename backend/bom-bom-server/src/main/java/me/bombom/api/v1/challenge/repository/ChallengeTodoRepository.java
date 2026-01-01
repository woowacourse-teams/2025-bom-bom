package me.bombom.api.v1.challenge.repository;

import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeTodoRepository extends JpaRepository<ChallengeTodo, Long> {
}
