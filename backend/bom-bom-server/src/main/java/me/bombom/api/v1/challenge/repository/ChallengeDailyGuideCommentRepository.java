package me.bombom.api.v1.challenge.repository;

import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuideComment;
import me.bombom.api.v1.challenge.dto.response.DailyGuideCommentResponse;
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
}

