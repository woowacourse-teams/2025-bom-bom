package me.bombom.api.v1.reading.repository;

import java.util.Optional;
import me.bombom.api.v1.reading.domain.MonthlyReadingRealtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MonthlyReadingRealtimeRepository extends JpaRepository<MonthlyReadingRealtime, Long> {

    Optional<MonthlyReadingRealtime> findByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MonthlyReadingRealtime mrr SET mrr.currentCount = 0")
    void resetCurrentCount();
}
