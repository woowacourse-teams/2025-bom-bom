package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.OngoingChallengeSummary;

@Schema(description = "참여 중 챌린지 1건 요약")
public record OngoingChallengeResponse(

        @Schema(description = "챌린지 식별자", example = "101")
        Long challengeId,

        @Schema(description = "챌린지 이름", example = "한 달 뉴스레터 읽기 챌린지")
        String title,

        @Schema(description = "챌린지 시작일")
        LocalDate startDate,

        @Schema(description = "챌린지 종료일")
        LocalDate endDate,

        @Schema(description = "종료까지 남은 일수 (D-N)", example = "14")
        int remainingDays,

        @Schema(description = "진행률 (= 출석률, %)", example = "72")
        int progressRate,

        @NotNull
        @Schema(description = "팀 내 나의 순위 정보")
        MyTeamRank myTeamRank,

        @NotNull
        @Schema(description = "우리 팀 순위 정보")
        TeamRank teamRank,

        @NotNull
        @Schema(description = "내 출석률 비교 정보")
        MyAttendanceComparison myAttendanceComparison,

        @NotNull
        @Schema(description = "팀 출석률 비교 정보")
        TeamAttendanceComparison teamAttendanceComparison
) {

    public static OngoingChallengeResponse from(OngoingChallengeSummary summary) {
        return new OngoingChallengeResponse(
                summary.challengeId(),
                summary.title(),
                summary.startDate(),
                summary.endDate(),
                summary.remainingDays(),
                summary.progressRate(),
                new MyTeamRank(summary.myTeamRank().rank(), summary.myTeamRank().total()),
                new TeamRank(summary.teamRank().rank(), summary.teamRank().total()),
                new MyAttendanceComparison(
                        summary.myAttendanceComparison().attendanceRate(),
                        summary.myAttendanceComparison().differencePoint()
                ),
                new TeamAttendanceComparison(
                        summary.teamAttendanceComparison().attendanceRate(),
                        summary.teamAttendanceComparison().differencePoint()
                )
        );
    }

    @Schema(description = "팀 내 나의 순위 정보")
    public record MyTeamRank(

            @Schema(description = "팀 내 나의 출석률 순위", example = "3")
            int rank,

            @Schema(description = "우리 팀 인원 수", example = "12")
            int totalMembers
    ) {
    }

    @Schema(description = "우리 팀 순위 정보 (팀 평균 출석률 기준)")
    public record TeamRank(

            @Schema(description = "팀 평균 출석률 기준 우리 팀 순위", example = "2")
            int rank,

            @Schema(description = "해당 챌린지의 팀 개수", example = "6")
            int totalTeams
    ) {
    }

    @Schema(description = "내 출석률 비교 정보")
    public record MyAttendanceComparison(

            @Schema(description = "현재 나의 출석률 (%)", example = "72")
            int attendanceRate,

            @Schema(description = "전체 참여자 평균 출석률 대비 차이 (%p, 부호 있음)", example = "6")
            int differencePoint
    ) {
    }

    @Schema(description = "팀 출석률 비교 정보")
    public record TeamAttendanceComparison(

            @Schema(description = "우리 팀 평균 출석률 (%)", example = "68")
            int teamAttendanceRate,

            @Schema(description = "전체 팀 평균 출석률 대비 차이 (%p, 부호 있음)", example = "4")
            int differencePoint
    ) {
    }
}
