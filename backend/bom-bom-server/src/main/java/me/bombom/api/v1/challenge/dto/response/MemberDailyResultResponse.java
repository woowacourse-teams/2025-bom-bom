package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.challenge.dto.DailyProgress;
import me.bombom.api.v1.challenge.dto.TeamChallengeProgressFlat;

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

    public static List<MemberDailyResultResponse> from(List<TeamChallengeProgressFlat> progressList) {
        Map<Long, MemberDailyResultResponse> map = new LinkedHashMap<>();
        for (TeamChallengeProgressFlat progress : progressList) {
            map.computeIfAbsent(progress.memberId(), memberId -> createResponse(progress))
                    .addDailyProgress(progress);
        }
        return new ArrayList<>(map.values());
    }

    private static MemberDailyResultResponse createResponse(TeamChallengeProgressFlat progress) {
        return new MemberDailyResultResponse(
                progress.memberId(),
                progress.nickname(),
                progress.isSurvived(),
                new ArrayList<>()
        );
    }

    private void addDailyProgress(TeamChallengeProgressFlat progress) {
        //null인 경우는 참여 기록이 없는 사용자 => 이름과 생존 여부만 표시
        if (progress.recordDate() != null) {
            this.dailyProgresses.add(new DailyProgress(progress.recordDate(), progress.status()));
        }
    }
}
