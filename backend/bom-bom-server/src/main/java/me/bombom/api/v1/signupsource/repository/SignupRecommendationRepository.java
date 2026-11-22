package me.bombom.api.v1.signupsource.repository;

import java.util.List;
import me.bombom.api.v1.signupsource.domain.SignupRecommendation;
import me.bombom.api.v1.signupsource.dto.response.NewsletterSubscriberCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SignupRecommendationRepository extends JpaRepository<SignupRecommendation, Long> {

    List<SignupRecommendation> findAllByOrderByDisplayOrderAsc();

    @Query("""
        SELECT new me.bombom.api.v1.signupsource.dto.response.NewsletterSubscriberCount(
            s.newsletterId,
            COUNT(s.id)
        )
        FROM Subscribe s
        WHERE s.newsletterId IN :newsletterIds
          AND s.status = me.bombom.api.v1.subscribe.domain.SubscribeStatus.SUBSCRIBED
        GROUP BY s.newsletterId
    """)
    List<NewsletterSubscriberCount> countSubscribersByNewsletterIds(@Param("newsletterIds") List<Long> newsletterIds);
}
