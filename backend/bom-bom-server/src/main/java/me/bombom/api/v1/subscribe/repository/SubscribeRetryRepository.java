package me.bombom.api.v1.subscribe.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.subscribe.domain.SubscribeRetry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscribeRetryRepository extends JpaRepository<SubscribeRetry, Long> {
    Optional<SubscribeRetry> findBySubscribeId(Long subscribeId);

    List<SubscribeRetry> findByNextRetryAtBefore(LocalDateTime now);
}
