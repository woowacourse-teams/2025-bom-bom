package me.bombom.api.v1.challenge.repository;

import me.bombom.api.v1.challenge.domain.ChallengeCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChallengeCommentLikeRepository extends JpaRepository<ChallengeCommentLike, Long> {

    @Modifying
    @Query(value = """
                    INSERT IGNORE INTO challenge_comment_like (participant_id, comment_id)
                    VALUES (:participantId, :commentId)
            """, nativeQuery = true)
    int insertIgnoreByParticipantIdAndCommentId(Long participantId, Long commentId);
}
