package me.bombom.api.v1.reading.repository;

import java.time.LocalDateTime;
import me.bombom.api.v1.reading.domain.MemberReadRateLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberReadRateLimitRepository extends JpaRepository<MemberReadRateLimit, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT IGNORE INTO member_read_rate_limit (member_id, tokens, updated_at)
            VALUES (:memberId, :bucketCapacity, :now)
    """, nativeQuery = true)
    void insertIfAbsent(
            @Param("memberId") Long memberId,
            @Param("bucketCapacity") int bucketCapacity,
            @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE member_read_rate_limit
            SET tokens = LEAST(:bucketCapacity, tokens + TIMESTAMPDIFF(SECOND, updated_at, :now) / :refillSeconds) - 1,
                updated_at = :now
            WHERE member_id = :memberId
              AND LEAST(:bucketCapacity, tokens + TIMESTAMPDIFF(SECOND, updated_at, :now) / :refillSeconds) >= 1
    """, nativeQuery = true)
    int tryConsume(
            @Param("memberId") Long memberId,
            @Param("bucketCapacity") int bucketCapacity,
            @Param("refillSeconds") int refillSeconds,
            @Param("now") LocalDateTime now
    );
}
