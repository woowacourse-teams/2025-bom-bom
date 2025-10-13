package news.bombomemail.subscribe.repository;

import java.util.Optional;
import news.bombomemail.subscribe.domain.NewsletterSubscriptionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsletterSubscriptionCountRepository extends JpaRepository<NewsletterSubscriptionCount, Long> {

    Optional<NewsletterSubscriptionCount> findByNewsletterId(Long newsletterId);

    /**
     * 원자적 연산으로 구독자 수 증가 - MySQL INSERT ON DUPLICATE KEY UPDATE
     *
     * @param newsletterId 뉴스레터 ID
     * @param ageGroup     연령대 컬럼명 (age0s, age10s, age20s, age30s, age40s, age50s, age60plus)
     */
    @Modifying(clearAutomatically = true)
    @Query(value = """
    INSERT INTO newsletter_subscription_count AS cur
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
        total    = cur.total    + 1,        
        age0s    = cur.age0s    + IF(:ageGroup = 'age0s', 1, 0),
        age10s   = cur.age10s   + IF(:ageGroup = 'age10s', 1, 0),
        age20s   = cur.age20s   + IF(:ageGroup = 'age20s', 1, 0),
        age30s   = cur.age30s   + IF(:ageGroup = 'age30s', 1, 0),
        age40s   = cur.age40s   + IF(:ageGroup = 'age40s', 1, 0),
        age50s   = cur.age50s   + IF(:ageGroup = 'age50s', 1, 0),
        age60plus= cur.age60plus+ IF(:ageGroup = 'age60plus', 1, 0)
    """, nativeQuery = true)
    void increaseSubscriptionCountByNewsletterIdAndAgeGroup(
            @Param("newsletterId") Long newsletterId,
            @Param("ageGroup") String ageGroup
    );
}
