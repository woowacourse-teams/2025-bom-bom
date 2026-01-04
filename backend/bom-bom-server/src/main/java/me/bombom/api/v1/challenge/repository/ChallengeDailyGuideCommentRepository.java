package me.bombom.api.v1.challenge.repository;

import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuideComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeDailyGuideCommentRepository extends JpaRepository<ChallengeDailyGuideComment, Long> {

    @Query("""
        SELECT c FROM ChallengeDailyGuideComment c
        WHERE c.guideId = :guideId
        AND c.participantId = :participantId
    """)
    Optional<ChallengeDailyGuideComment> findByGuideIdAndParticipantId(
            @Param("guideId") Long guideId,
            @Param("participantId") Long participantId
    );
}

