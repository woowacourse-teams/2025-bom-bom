package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.challenge.dto.DailyProgress;

public record MemberDailyResultResponse(

        @NotNull
        Long memberId,

        @NotNull
        String nickname,

        @Schema(required = true)
        boolean isSurvived,

        @NotNull
        List<DailyProgress> dailyProgresses
) {
}
