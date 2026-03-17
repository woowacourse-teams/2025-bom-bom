package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.challenge.dto.ChallengeProgressFlat;
import me.bombom.api.v1.member.domain.Member;

public record MemberChallengeProgressResponse(

        @NotNull
        String nickname,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        int totalDays,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        boolean isSurvived,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        int completedDays,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        int streak,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        int shield,

        @NotNull
        List<TodayTodoResponse> todayTodos
) {

    public static MemberChallengeProgressResponse of(Member member, List<ChallengeProgressFlat> progressList) {
        ChallengeProgressFlat representative = progressList.getFirst();

        List<TodayTodoResponse> todayTodos = progressList.stream()
                .map(TodayTodoResponse::from)
                .toList();

        return new MemberChallengeProgressResponse(
                member.getNickname(),
                representative.totalDays(),
                representative.isSurvived(),
                representative.completedDays(),
                representative.streak(),
                representative.shield(),
                todayTodos
        );
    }
}
