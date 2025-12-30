package me.bombom.api.v1.challenge.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeCommentRepository extends JpaRepository<ChallengeComment, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.response.ChallengeCommentResponse(
            m.nickname,
            n.name,
            CASE
                WHEN s.id IS NULL THEN false
                ELSE true
            END,
            a.title,
            cc.quotation,
            cc.comment,
            cc.createdAt
        )
        FROM ChallengeComment cc
        JOIN ChallengeParticipant cp ON cc.participantId = cp.id
        JOIN Member m ON cp.memberId = m.id
        JOIN Article a ON cc.articleId = a.id
        JOIN Newsletter n ON a.newsletterId = n.id
        LEFT JOIN Subscribe s ON s.newsletterId = n.id AND s.memberId = :currentMemberId
        WHERE cp.challengeTeamId = :teamId
        AND FUNCTION('DATE', cc.createdAt) BETWEEN :startDate AND :endDate
    """)
    Page<ChallengeCommentResponse> findAllByTeamInDuration(
            @Param("teamId") Long teamId,
            @Param("currentMemberId") Long currentMemberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
