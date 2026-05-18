package me.bombom.api.v1.reading.repository;

import java.time.LocalDateTime;
import me.bombom.api.v1.reading.domain.MemberReadTokenBucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberReadTokenBucketRepository extends JpaRepository<MemberReadTokenBucket, Long> {

    @Modifying(flushAutomatically = true)
    // TODO: 현재는 PK(member_id) 충돌만 의도하지만, unique 제약 추가 시 INSERT IGNORE 재검토 필요
    @Query(value = """
            INSERT IGNORE INTO member_read_token_bucket (member_id, tokens, updated_at)
            VALUES (:memberId, :bucketCapacity, :now)
    """, nativeQuery = true)
    void insertIfAbsent(
            @Param("memberId") Long memberId,
            @Param("bucketCapacity") int bucketCapacity,
            @Param("now") LocalDateTime now
    );

    @Modifying(flushAutomatically = true)
    @Query(value = """
            UPDATE member_read_token_bucket
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
