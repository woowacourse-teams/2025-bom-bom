package me.bombom.api.v1.reading.repository;

import java.util.Optional;
import me.bombom.api.v1.reading.domain.MonthlyReadingRealtime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyReadingRealtimeRepository extends JpaRepository<MonthlyReadingRealtime, Long> {

    Optional<MonthlyReadingRealtime> findByMemberId(Long memberId);
}
