package me.bombom.api.v1.challenge.dto;

import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;

public record TeamChallengeProgressFlat(

        // 멤버 정보
        Long memberId,
        String nickname,
        boolean isSurvived,

        // 챌린지 진행 정보
        int completedDays,
        int totalDays,
        int teamProgress,

        // 일자별 기록 정보
        LocalDate recordDate,
        ChallengeDailyStatus status
) {
}
