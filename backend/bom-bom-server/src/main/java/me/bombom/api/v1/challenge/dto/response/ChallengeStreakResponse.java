package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;

public record ChallengeStreakResponse(

        @Schema(requiredMode = RequiredMode.REQUIRED)
        int streak,

        @NotNull
        List<StreakDayResponse> streakDays
) {

    public static ChallengeStreakResponse empty() {
        return new ChallengeStreakResponse(0, List.of());
    }

    public static ChallengeStreakResponse of(int streak, List<ChallengeDailyResult> results) {
        List<StreakDayResponse> days = StreakDayResponse.from(results.reversed());
        return new ChallengeStreakResponse(streak, days);
    }

    public static ChallengeStreakResponse withTodayAbsent(int streak, List<ChallengeDailyResult> results, LocalDate today) {
        List<StreakDayResponse> days = new ArrayList<>(StreakDayResponse.from(results.reversed()));
        days.add(StreakDayResponse.notParticipated(today));
        return new ChallengeStreakResponse(streak, days);
    }
}
