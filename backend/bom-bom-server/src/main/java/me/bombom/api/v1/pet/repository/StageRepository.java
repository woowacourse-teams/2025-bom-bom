package me.bombom.api.v1.pet.repository;

import me.bombom.api.v1.pet.domain.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StageRepository extends JpaRepository<Stage, Long> {
}
