package me.bombom.api.v1.reading.repository;

import java.util.Optional;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContinueReadingRealtimeRepository extends JpaRepository<ContinueReadingRealtime, Long> {

    Optional<ContinueReadingRealtime> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
