package me.bombom.api.v1.challenge.repository;

import me.bombom.api.v1.challenge.domain.ChallengeCommentReply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeCommentReplyRepository extends JpaRepository<ChallengeCommentReply, Long> {
}
