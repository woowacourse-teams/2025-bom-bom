package me.bombom.api.v1.member.repository;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import me.bombom.api.v1.member.domain.ContinueReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContinueReadingRepository extends JpaRepository<ContinueReading, Long> {

    Optional<ContinueReading> findByMemberId(Long memberId);
}
