package me.bombom.api.v1.member.repository;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import me.bombom.api.v1.member.domain.TodayReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodayReadingRepository extends JpaRepository<TodayReading, Long> {

    Optional<TodayReading> findByMemberId(Long memberId);
}
