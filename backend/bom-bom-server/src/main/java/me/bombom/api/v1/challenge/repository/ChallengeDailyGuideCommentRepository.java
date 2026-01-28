package me.bombom.api.v1.challenge.repository;

import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuideComment;
import me.bombom.api.v1.challenge.dto.response.DailyGuideCommentResponse;
import me.bombom.api.v1.challenge.dto.response.MemberDailyCommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeDailyGuideCommentRepository extends JpaRepository<ChallengeDailyGuideComment, Long> {

    Optional<ChallengeDailyGuideComment> findByGuideIdAndParticipantId(Long guideId, Long participantId);

    @Query("""
            SELECT new me.bombom.api.v1.challenge.dto.response.DailyGuideCommentResponse(
                            m.nickname,
                            c.content,
                            c.createdAt
                        )
            FROM ChallengeDailyGuideComment c
            JOIN ChallengeParticipant cp on cp.id = c.participantId
            JOIN Member m on m.id = cp.memberId
            WHERE c.guideId = :guideId
            """)
    Page<DailyGuideCommentResponse> findByGuideId(@Param("guideId") Long guideId, Pageable pageable);

    @Query("""
            SELECT new me.bombom.api.v1.challenge.dto.response.MemberDailyCommentResponse(c.content)
            FROM ChallengeParticipant p
            JOIN ChallengeDailyGuide g ON g.challengeId = p.challengeId AND g.dayIndex = :dayIndex
            LEFT JOIN ChallengeDailyGuideComment c ON c.participantId = p.id AND c.guideId = g.id
            WHERE p.challengeId = :challengeId
            AND p.memberId = :memberId
    """)
    MemberDailyCommentResponse findMyComment(
            @Param("challengeId") Long challengeId,
            @Param("dayIndex") int dayIndex,
            @Param("memberId") Long memberId
    );
}
