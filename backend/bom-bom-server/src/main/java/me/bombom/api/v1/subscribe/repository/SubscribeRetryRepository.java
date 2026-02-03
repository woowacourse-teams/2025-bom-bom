package me.bombom.api.v1.subscribe.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.subscribe.domain.SubscribeRetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscribeRetryRepository extends JpaRepository<SubscribeRetry, Long> {
    Optional<SubscribeRetry> findBySubscribeId(Long subscribeId);

    @Query(value = """
            SELECT sr
            FROM SubscribeRetry sr
            WHERE sr.nextRetryAt < :now
            ORDER BY sr.nextRetryAt
            LIMIT :limit
    """)
    List<SubscribeRetry> findPendingRetries(
            @Param("now") LocalDateTime now,
            @Param("limit") int limit
    );
}
