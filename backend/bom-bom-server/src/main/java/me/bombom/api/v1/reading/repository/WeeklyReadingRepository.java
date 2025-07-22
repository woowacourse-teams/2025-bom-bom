package me.bombom.api.v1.reading.repository;

import java.util.Optional;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyReadingRepository extends JpaRepository<WeeklyReading, Long> {

    Optional<WeeklyReading> findByMemberId(Long memberId);
}
