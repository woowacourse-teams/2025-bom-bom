package me.bombom.api.v1.challenge.repository;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeCommentRepository extends JpaRepository<ChallengeComment, Long> {

    @Query("""
                SELECT new me.bombom.api.v1.challenge.dto.response.ChallengeCommentResponse(
                    cc.id,
                    m.nickname,
                    m.profileImageUrl,
                    n.name,
                    CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM Subscribe s
                            WHERE s.newsletterId = cc.newsletterId
                              AND s.memberId = :currentMemberId
                        ) THEN true
                        ELSE false
                    END,
                    cc.articleTitle,
                    cc.quotation,
                    cc.comment,
                    cc.createdAt,
                    CASE
                        WHEN cp.memberId = :currentMemberId THEN true
                        ELSE false
                    END,
                    cc.likeCount,
                    CASE
                        WHEN ccl.id IS NOT NULL THEN true
                        ELSE false
                    END,
                    (
                        SELECT COUNT(cr.id)
                        FROM ChallengeCommentReply cr
                        JOIN ChallengeParticipant crAuthor ON cr.participantId = crAuthor.id
                        WHERE cr.commentId = cc.id
                          AND (
                              cr.isPrivate = false
                              OR crAuthor.memberId = :currentMemberId
                              OR cp.memberId = :currentMemberId
                          )
                    )
                )
                FROM ChallengeComment cc
                JOIN ChallengeParticipant cp ON cc.participantId = cp.id
                LEFT JOIN ChallengeParticipant myCp
                    ON myCp.challengeId = cp.challengeId AND myCp.memberId = :currentMemberId
                LEFT JOIN ChallengeCommentLike ccl
                    ON ccl.commentId = cc.id AND ccl.participantId = myCp.id
                LEFT JOIN Member m ON cp.memberId = m.id
                JOIN Newsletter n ON cc.newsletterId = n.id
                WHERE cp.challengeId = :challengeId
                AND FUNCTION('DATE', cc.createdAt) BETWEEN :startDate AND :endDate
            """)
    Page<ChallengeCommentResponse> findAllInDuration(
            @Param("challengeId") Long challengeId,
            @Param("currentMemberId") Long currentMemberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                UPDATE ChallengeComment c
                   SET c.likeCount = c.likeCount + :amount
                 WHERE c.id = :commentId
                   AND ( :amount >= 0 OR c.likeCount > 0 )
            """)
    void bulkIncrementLikeCountNotBelowZero(Long commentId, int amount);

    @Modifying(flushAutomatically = true)
    @Query("""
                UPDATE ChallengeComment c
                   SET c.replyCount = c.replyCount + 1
                 WHERE c.id = :commentId
            """)
    void bulkUpdateReplyCount(Long commentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
                UPDATE challenge_comment cc
                JOIN (
                    SELECT comment_id, COUNT(*) AS cnt
                    FROM challenge_comment_like
                    WHERE participant_id IN (:participantIds)
                    GROUP BY comment_id
                ) l ON l.comment_id = cc.id
                SET cc.like_count = GREATEST(cc.like_count - l.cnt, 0)
            """, nativeQuery = true)
    void bulkDecreaseLikeCountByParticipantIds(@Param("participantIds") List<Long> participantIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
                UPDATE challenge_comment cc
                JOIN (
                    SELECT comment_id, COUNT(*) AS cnt
                    FROM challenge_comment_reply
                    WHERE participant_id IN (:participantIds)
                    GROUP BY comment_id
                ) r ON r.comment_id = cc.id
                SET cc.reply_count = GREATEST(cc.reply_count - r.cnt, 0)
            """, nativeQuery = true)
    void bulkDecreaseReplyCountByParticipantIds(@Param("participantIds") List<Long> participantIds);

    @Query("""
            SELECT cc.id
            FROM ChallengeComment cc
            WHERE cc.participantId IN :participantIds
            """)
    List<Long> findIdsByParticipantIdIn(@Param("participantIds") List<Long> participantIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM ChallengeComment cc
            WHERE cc.participantId IN :participantIds
            """)
    void bulkDeleteAllByParticipantIdIn(@Param("participantIds") List<Long> participantIds);
}
