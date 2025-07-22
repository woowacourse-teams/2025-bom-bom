package me.bombom.api.v1.reading.repository;

import java.util.Optional;
import me.bombom.api.v1.reading.domain.TodayReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodayReadingRepository extends JpaRepository<TodayReading, Long> {

    Optional<TodayReading> findByMemberId(Long memberId);
}
