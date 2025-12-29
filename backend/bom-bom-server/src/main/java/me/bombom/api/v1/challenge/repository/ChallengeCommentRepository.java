package me.bombom.api.v1.challenge.repository;

import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.ChallengeComment;
import me.bombom.api.v1.challenge.dto.ChallengeCommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeCommentRepository extends JpaRepository<ChallengeComment, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.ChallengeCommentResponse(
            m.nickname,
            n.name,
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
        WHERE cp.challengeTeamId = :teamId
        AND FUNCTION('DATE', cc.createdAt) BETWEEN :startDate AND :endDate
    """)
    Page<ChallengeCommentResponse> findAllByTeamInDuration(
            @Param("teamId") Long teamId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
