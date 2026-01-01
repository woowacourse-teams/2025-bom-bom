package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.challenge.dto.ChallengeProgressFlat;
import me.bombom.api.v1.member.domain.Member;

public record MemberChallengeProgressResponse(

        @NotNull
        String nickname,

        @Schema(required = true)
        int totalDays,

        @Schema(required = true)
        boolean isSurvived,

        @Schema(required = true)
        int completedDays,

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
                todayTodos
        );
    }
}
