package me.bombom.api.v1.challenge.repository;

import me.bombom.api.v1.challenge.domain.ChallengeCommentReply;
import me.bombom.api.v1.challenge.dto.response.CommentReplyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeCommentReplyRepository extends JpaRepository<ChallengeCommentReply, Long> {

    @Query("""
                SELECT new me.bombom.api.v1.challenge.dto.response.CommentReplyResponse(
                    cr.id,
                    crMember.nickname,
                    crMember.profileImageUrl,
                    cr.reply,
                    cr.createdAt,
                    CASE WHEN crAuthor.memberId = :memberId THEN true ELSE false END,
                    cr.isPrivate,
                    CASE
                        WHEN cr.isPrivate = false THEN true
                        WHEN crAuthor.memberId = :memberId THEN true
                        WHEN commentAuthor.memberId = :memberId THEN true
                        ELSE false
                    END
                )
                FROM ChallengeCommentReply cr
                JOIN ChallengeParticipant crAuthor ON cr.participantId = crAuthor.id
                LEFT JOIN Member crMember ON crAuthor.memberId = crMember.id
                JOIN ChallengeComment comment ON cr.commentId = comment.id
                JOIN ChallengeParticipant commentAuthor ON comment.participantId = commentAuthor.id
                WHERE cr.commentId = :commentId
                  AND (
                        cr.isPrivate = false
                        OR crAuthor.memberId = :memberId
                        OR commentAuthor.memberId = :memberId
                  )
            """)
    Page<CommentReplyResponse> findAllByCommentId(
            @Param("commentId") Long commentId,
            @Param("memberId") Long memberId,
            Pageable pageable
    );
}
