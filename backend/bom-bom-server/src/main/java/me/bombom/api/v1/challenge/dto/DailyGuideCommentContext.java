package me.bombom.api.v1.challenge.dto;

import me.bombom.api.v1.challenge.domain.ChallengeDailyGuide;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;

public record DailyGuideCommentContext(

        Long challengeId,
        int dayIndex,
        Long memberId,
        ChallengeParticipant participant,
        ChallengeDailyGuide guide
) {
}
