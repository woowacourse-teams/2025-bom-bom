package me.bombom.api.v1.image.repository;

import java.time.LocalDateTime;
import me.bombom.api.v1.image.domain.ImageUploadTokenBucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageUploadTokenBucketRepository extends JpaRepository<ImageUploadTokenBucket, String> {

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO image_upload_token_bucket (bucket_key, tokens, updated_at)
            VALUES (:bucketKey, :bucketCapacity, :now)
    """, nativeQuery = true)
    void insertIfAbsent(
            @Param("bucketKey") String bucketKey,
            @Param("bucketCapacity") int bucketCapacity,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query(value = """
            UPDATE image_upload_token_bucket
            SET tokens = LEAST(:bucketCapacity, tokens + TIMESTAMPDIFF(SECOND, updated_at, :now) / :refillSeconds) - :cost,
                updated_at = :now
            WHERE bucket_key = :bucketKey
              AND LEAST(:bucketCapacity, tokens + TIMESTAMPDIFF(SECOND, updated_at, :now) / :refillSeconds) >= :cost
    """, nativeQuery = true)
    int refillAndTryConsume(
            @Param("bucketKey") String bucketKey,
            @Param("bucketCapacity") int bucketCapacity,
            @Param("refillSeconds") int refillSeconds,
            @Param("cost") int cost,
            @Param("now") LocalDateTime now
    );

    /**
     * updated_at은 갱신하지 않는다. 갱신하면 차감 시점부터 환불 시점까지의 회복분이 사라진다.
     */
    @Modifying
    @Query(value = """
            UPDATE image_upload_token_bucket
            SET tokens = LEAST(:bucketCapacity, tokens + :cost)
            WHERE bucket_key = :bucketKey
    """, nativeQuery = true)
    int refund(
            @Param("bucketKey") String bucketKey,
            @Param("bucketCapacity") int bucketCapacity,
            @Param("cost") int cost
    );
}
