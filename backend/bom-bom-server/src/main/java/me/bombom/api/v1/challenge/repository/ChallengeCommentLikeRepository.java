package me.bombom.api.v1.challenge.repository;

import me.bombom.api.v1.challenge.domain.ChallengeCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeCommentLikeRepository extends JpaRepository<ChallengeCommentLike, Long> {
    void updateLike(Long memberId, Long commentId);
}
