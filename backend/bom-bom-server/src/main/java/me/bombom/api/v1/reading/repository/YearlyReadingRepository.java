package me.bombom.api.v1.reading.repository;

import java.util.Optional;
import me.bombom.api.v1.reading.domain.YearlyReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YearlyReadingRepository extends JpaRepository<YearlyReading, Long> {
    Optional<YearlyReading> findByMemberIdAndYear(Long memberId, int year);
}
