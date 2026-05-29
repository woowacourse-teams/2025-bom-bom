package me.bombom.api.v1.subscribe.repository;

import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsletterSubscriptionCountRepository extends JpaRepository<NewsletterSubscriptionCount, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = """
            INSERT INTO newsletter_subscription_count
                (newsletter_id, total, age0s, age10s, age20s, age30s, age40s, age50s, age60plus)
            VALUES (
                :newsletterId,
                1,
                IF(:ageGroup = 'age0s', 1, 0),
                IF(:ageGroup = 'age10s', 1, 0),
                IF(:ageGroup = 'age20s', 1, 0),
                IF(:ageGroup = 'age30s', 1, 0),
                IF(:ageGroup = 'age40s', 1, 0),
                IF(:ageGroup = 'age50s', 1, 0),
                IF(:ageGroup = 'age60plus', 1, 0)
            )
            ON DUPLICATE KEY UPDATE
                total     = total     + 1,
                age0s     = age0s     + IF(:ageGroup = 'age0s', 1, 0),
                age10s    = age10s    + IF(:ageGroup = 'age10s', 1, 0),
                age20s    = age20s    + IF(:ageGroup = 'age20s', 1, 0),
                age30s    = age30s    + IF(:ageGroup = 'age30s', 1, 0),
                age40s    = age40s    + IF(:ageGroup = 'age40s', 1, 0),
                age50s    = age50s    + IF(:ageGroup = 'age50s', 1, 0),
                age60plus = age60plus + IF(:ageGroup = 'age60plus', 1, 0)
            """, nativeQuery = true)
    void bulkIncreaseSubscriptionCountByNewsletterIdAndAgeGroup(
            @Param("newsletterId") Long newsletterId,
            @Param("ageGroup") String ageGroup
    );

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE newsletter_subscription_count
            SET total     = GREATEST(total - 1, 0),
                age0s     = GREATEST(age0s - IF(:ageGroup = 'age0s', 1, 0), 0),
                age10s    = GREATEST(age10s - IF(:ageGroup = 'age10s', 1, 0), 0),
                age20s    = GREATEST(age20s - IF(:ageGroup = 'age20s', 1, 0), 0),
                age30s    = GREATEST(age30s - IF(:ageGroup = 'age30s', 1, 0), 0),
                age40s    = GREATEST(age40s - IF(:ageGroup = 'age40s', 1, 0), 0),
                age50s    = GREATEST(age50s - IF(:ageGroup = 'age50s', 1, 0), 0),
                age60plus = GREATEST(age60plus - IF(:ageGroup = 'age60plus', 1, 0), 0)
            WHERE newsletter_id = :newsletterId
            """, nativeQuery = true)
    void bulkDecreaseSubscriptionCountByNewsletterIdAndAgeGroup(
            @Param("newsletterId") Long newsletterId,
            @Param("ageGroup") String ageGroup
    );
}
