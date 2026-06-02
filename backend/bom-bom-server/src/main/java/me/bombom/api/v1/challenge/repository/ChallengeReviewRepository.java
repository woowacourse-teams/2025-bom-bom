package me.bombom.api.v1.challenge.repository;

import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeReview;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeReviewRepository extends JpaRepository<ChallengeReview, Long> {

    Optional<ChallengeReview> findByChallengeIdAndMemberId(Long challengeId, Long memberId);

    boolean existsByChallengeIdAndMemberId(Long challengeId, Long memberId);

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.response.ChallengeReviewListItem(
            cr.id,
            m.nickname,
            cr.comment,
            cr.isPrivate
        )
        FROM ChallengeReview cr
        LEFT JOIN Member m ON m.id = cr.memberId
        WHERE cr.challengeId = :challengeId
          AND cr.memberId <> :viewerMemberId
          AND cr.isPrivate = false
    """)
    Page<ChallengeReviewListItem> findVisibleReviews(
            @Param("challengeId") Long challengeId,
            @Param("viewerMemberId") Long viewerMemberId,
            Pageable pageable
    );
}
