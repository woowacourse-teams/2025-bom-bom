package me.bombom.api.v1.reading.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingCountResponse;
import me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.MonthlyReadingRankingResponse;
import me.bombom.api.v1.reading.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.reading.domain.StreakPeriod;
import me.bombom.api.v1.reading.dto.response.RecentContinueReadingResponse;
import me.bombom.api.v1.reading.dto.response.ContinueReadingRankingResponse;
import me.bombom.api.v1.reading.dto.response.MemberContinueReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.WeeklyGoalCountResponse;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Reading", description = "읽기 정보 관련 API")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content)
})
public interface ReadingControllerApi {

    @Operation(summary = "읽기 정보 조회", description = "현재 사용자의 주간/오늘/연속 읽기 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "읽기 정보 조회 성공")
    })
    ReadingInformationResponse getReadingInformation(@Parameter(hidden = true) Member member);

    @Operation(summary = "주간 목표 아티클 개수 수정", description = "주간 목표로 설정된 아티클 개수를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주간 목표 개수 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
        @ApiResponse(responseCode = "404", description = "주간 읽기 정보를 찾을 수 없음", content = @Content)
    })
    WeeklyGoalCountResponse updateWeeklyGoalCount(
            @Parameter(hidden = true) Member member,
            @NotNull(message = "주간 목표 개수는 필수 입력 값입니다.") @Positive(message = "주간 목표 개수는 양수여야 합니다.") Integer weeklyGoalCount
    );

    @Operation(
            summary = "이달의 독서왕 조회",
            description = "현재 읽기 카운트를 기준으로 내림차순하여 순위와 함께 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이달의 독서왕 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값 (limit는 1 이상의 값이어야 함)", content = @Content),
    })
    MonthlyReadingRankingResponse getMonthlyReadingRank(
            @Parameter(description = "최대 조회 개수 (예: ?limit=10)") @RequestParam @Positive(message = "limit는 1 이상의 값이어야 합니다.") int limit);

    @Operation(
            summary = "스트릭 랭킹 조회",
            description = "연속 읽기 일수(continue_reading_realtime.day_count) 기준 내림차순 순위를 조회합니다. day_count가 0인 회원도 목록에 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스트릭 랭킹 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값 (limit는 1 이상의 값이어야 함)", content = @Content),
    })
    ContinueReadingRankingResponse getContinueReadingRank(
            @Parameter(description = "최대 조회 개수 (예: ?limit=10)") @RequestParam @Positive(message = "limit는 1 이상의 값이어야 합니다.") int limit
    );

    @Operation(summary = "나의 월간 순위 조회", description = "저장된 rank 기반으로 나의 순위와 총 랭킹 참여자 수를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "나의 월간 순위 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
    })
    MemberMonthlyReadingRankResponse getMemberMonthlyRank(@Parameter(hidden = true) Member member);

    @Operation(
            summary = "나의 스트릭 순위 조회",
            description = "실시간 연속 읽기 일수 기준 나의 순위와 연속 읽기 보호막 현황을 반환합니다. day_count가 0이면 월간 순위와 같이 최하위 구간의 공동 순위로 포함됩니다. continue_reading_snapshot 행이 없으면 404입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "나의 스트릭 순위 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
            @ApiResponse(responseCode = "404", description = "연속 읽기 랭킹 스냅샷 정보 없음", content = @Content),
    })
    MemberContinueReadingRankResponse getMemberContinueReadingRank(@Parameter(hidden = true) Member member);

    @Operation(
            summary = "나의 최근 연속 읽기 기록 조회",
            description = """
                    오늘을 포함한 최근 days일의 날짜별 연속 읽기 상태와 현재 연속 일수·순위·보호막 현황을 반환합니다.
                    dayCount는 실시간 값이고, rank는 랭킹 스냅샷 값이라 약 10분 늦을 수 있습니다. streakShield는 /streak/rank/me와 같습니다.
                    날짜는 KST 기준이며 오름차순입니다.
                    status 판정 순서: READ → PENDING(오늘, 아직 안 읽음) → BEFORE_JOIN(가입일 이전) → SHIELD(보호막 사용) → REST(주말·공휴일) → NO_ARTICLE(받은 아티클이 없는 평일) → MISSED.
                    READ는 연속 읽기 계산이 실제로 쓴 값입니다. 오늘은 today_reading, 지난 날짜는 매일 0시 초기화 때 기록한 결과(90일 보관)를 씁니다.
                    기록이 시작되기 전 날짜는 읽은 기록으로 대신 판정하므로, 받은 아티클이 없던 평일도 MISSED로 보일 수 있습니다.
                    0시 초기화가 끝나기 전 잠깐 동안은 어제가 MISSED로 보일 수 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최근 연속 읽기 기록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값 (days는 1 이상 31 이하)", content = @Content),
            @ApiResponse(responseCode = "404", description = "연속 읽기 랭킹 스냅샷 정보 없음", content = @Content),
    })
    RecentContinueReadingResponse getRecentContinueReading(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "조회할 일수, 오늘 포함 (기본 7, 1~31). 응답의 days 목록 길이가 된다", example = "7")
            @RequestParam(defaultValue = StreakPeriod.DEFAULT_DAYS)
            @Min(value = StreakPeriod.MIN_DAYS, message = StreakPeriod.DAYS_RANGE_MESSAGE)
            @Max(value = StreakPeriod.MAX_DAYS, message = StreakPeriod.DAYS_RANGE_MESSAGE)
            int days
    );

    @Operation(summary = "나의 월간 읽기 개수 조회", description = "현재 로그인한 사용자의 이번 달 아티클 읽기 개수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "월간 읽기 개수 조회 성공"),
        @ApiResponse(responseCode = "404", description = "월간 읽기 정보를 찾을 수 없음", content = @Content)
    })
    MemberMonthlyReadingCountResponse getMemberMonthlyReadingCount(@Parameter(hidden = true) Member member);
}
