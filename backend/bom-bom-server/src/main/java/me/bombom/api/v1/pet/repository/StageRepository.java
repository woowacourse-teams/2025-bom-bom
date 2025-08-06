package me.bombom.api.v1.pet.repository;

import java.util.List;
import java.util.Optional;
import javax.swing.text.html.Option;
import me.bombom.api.v1.pet.domain.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StageRepository extends JpaRepository<Stage, Long> {


    @Query("""
                SELECT s
                FROM Stage s
                WHERE s.requiredScore <= :score
                ORDER BY s.requiredScore DESC
                LIMIT 1
            """)
    Optional<Stage> findFirstByScore(@Param("score") int score);
}
