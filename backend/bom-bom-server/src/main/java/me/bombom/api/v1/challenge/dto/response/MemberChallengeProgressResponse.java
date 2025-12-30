package me.bombom.api.v1.challenge.dto.response;

import java.util.List;
import me.bombom.api.v1.challenge.dto.ChallengeProgressFlat;
import me.bombom.api.v1.member.domain.Member;

public record MemberChallengeProgressResponse(

        String nickname,
        int totalDays,
        int completedDays,
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
                representative.completedDays(),
                todayTodos
        );
    }
}
