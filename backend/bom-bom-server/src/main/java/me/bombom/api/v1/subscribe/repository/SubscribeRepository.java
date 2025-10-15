package me.bombom.api.v1.subscribe.repository;

import java.util.List;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.dto.SubscribedNewsletterResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.subscribe.dto.SubscribedNewsletterResponse(
            n.name, n.imageUrl, c.name
        )
        FROM Subscribe s
        JOIN Newsletter n ON s.newsletterId = n.id
        JOIN Category c ON n.categoryId = c.id
        WHERE s.memberId = :memberId
    """)
    List<SubscribedNewsletterResponse> findSubscribedByMemberId(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Subscribe sb WHERE sb.memberId = :memberId")
    void deleteAllByMemberId(Long memberId);
}
