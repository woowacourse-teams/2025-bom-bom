package me.bombom.api.v1.challenge.repository;

import java.util.Optional;
import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeDailyGuideRepository extends JpaRepository<ChallengeDailyGuide, Long> {

    @Query(value = """
            select
                g.day_index as dayIndex,
                g.type as type,
                g.image_url as imageUrl,
                g.notice as notice,
                g.comment_enabled as commentEnabled,
                case when cm.id is null then 0 else 1 end as myCommentExists,
                cm.content as myCommentContent,
                cm.created_at as myCommentCreatedAt
            from challenge_daily_guide g
            left join challenge_participant cp
              on cp.challenge_id = :challengeId
             and cp.member_id = :memberId
             and cp.is_survived = true
            left join challenge_daily_guide_comment cm
              on cm.guide_id = g.id
             and cm.participant_id = cp.id
            where g.challenge_id = :challengeId
              and g.day_index = :dayIndex
            """, nativeQuery = true)
    Optional<TodayDailyGuideRow> findTodayGuide(
            @Param("challengeId") Long challengeId,
            @Param("memberId") Long memberId,
            @Param("dayIndex") int dayIndex
    );

    Optional<ChallengeDailyGuide> findByChallengeIdAndDayIndex(Long challengeId, int dayIndex);
}
