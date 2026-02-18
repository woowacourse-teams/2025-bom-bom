package me.bombom.api.v1.challenge.repository;

import java.util.List;
import me.bombom.api.v1.challenge.domain.notification.ChallengeStartNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeStartNotificationRepository extends JpaRepository<ChallengeStartNotification, Long> {

    @Query("""
                SELECT n.memberId
                FROM ChallengeStartNotification n
                WHERE n.challengeId = :challengeId
            """)
    List<Long> findMemberIdsByChallengeId(@Param("challengeId") Long challengeId);
}
