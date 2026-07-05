package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.challenge.domain.MedalCounts;
import me.bombom.api.v1.challenge.domain.MyChallengeSummary;

public record MyChallengeSummaryResponse(

        @Schema(description = "완료한 챌린지 수", example = "5")
        int completedChallengeCount,

        @NotNull
        @Schema(description = "수료율 순위 정보")
        CompletionRank completionRank,

        @NotNull
        @Schema(description = "출석률 순위 정보")
        AttendanceRank attendanceRank,

        @NotNull
        @Schema(description = "메달 획득 비율")
        MedalRatioResponse medalRatio
) {

    public static MyChallengeSummaryResponse from(MyChallengeSummary summary) {
        return new MyChallengeSummaryResponse(
                summary.completedChallengeCount(),
                new CompletionRank(summary.completionTopPercent(), summary.completionRate()),
                new AttendanceRank(summary.attendanceTopPercent(), summary.averageAttendanceRate()),
                MedalRatioResponse.from(summary.medalCounts())
        );
    }

    @Schema(description = "수료율 순위 정보 (다른 회원 대비 상대 순위)")
    public record CompletionRank(

            @Schema(description = "상위 몇 % 인지 (소수 첫째자리)", example = "18.0")
            double topPercent,

            @Schema(description = "나의 수료율 (%)", example = "80")
            int completionRate
    ) {
    }

    @Schema(description = "챌린지 출석률 순위 정보 (다른 회원 대비 상대 순위)")
    public record AttendanceRank(

            @Schema(description = "상위 몇 % 인지 (소수 첫째자리)", example = "7.0")
            double topPercent,

            @Schema(description = "나의 평균 챌린지 출석률 (%)", example = "87")
            int averageAttendanceRate
    ) {
    }

    @Schema(description = "메달 획득 비율 (%) - 소수점 버림이라 합은 100 이하 가능, 수료 0건이면 0/0/0")
    public record MedalRatioResponse(

            @Schema(description = "금메달 획득 비율 (%)", example = "40")
            int gold,

            @Schema(description = "은메달 획득 비율 (%)", example = "35")
            int silver,

            @Schema(description = "동메달 획득 비율 (%)", example = "25")
            int bronze
    ) {

        /**
         * 등급별 개수를 비율(%)로 변환한다. 소수점은 버림하므로 합이 100 이하일 수 있다.
         */
        public static MedalRatioResponse from(MedalCounts counts) {
            int total = counts.total();
            if (total == 0) {
                return new MedalRatioResponse(0, 0, 0);
            }
            return new MedalRatioResponse(
                    counts.getGold() * 100 / total,
                    counts.getSilver() * 100 / total,
                    counts.getBronze() * 100 / total
            );
        }
    }
}
