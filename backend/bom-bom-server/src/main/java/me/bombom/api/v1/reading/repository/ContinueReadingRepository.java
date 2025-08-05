package me.bombom.api.v1.reading.repository;

import java.util.Optional;
import me.bombom.api.v1.reading.domain.ContinueReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContinueReadingRepository extends JpaRepository<ContinueReading, Long> {

    Optional<ContinueReading> findByMemberId(Long memberId);
}
