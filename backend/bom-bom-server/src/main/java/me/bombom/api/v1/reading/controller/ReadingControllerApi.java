package me.bombom.api.v1.reading.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.reading.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.MonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.reading.dto.response.WeeklyGoalCountResponse;
import org.springframework.web.bind.annotation.RequestBody;
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
    WeeklyGoalCountResponse updateWeeklyGoalCount(@Valid @RequestBody UpdateWeeklyGoalCountRequest request);

    @Operation(
            summary = "이달의 독서왕 조회",
            description = "현재 읽기 카운트를 기준으로 내림차순하여 순위와 함께 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이달의 독서왕 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
    })
    List<MonthlyReadingRankResponse> getMonthlyReadingRank(
            @Parameter(description = "최대 조회 개수 (예: ?limit=10)") @RequestParam @Positive(message = "limit는 1 이상의 값이어야 합니다.") int limit);

    @Operation(summary = "나의 월간 순위 조회", description = "저장된 rank 기반으로 나의 순위와 총 랭킹 참여자 수를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "나의 월간 순위 조회 성공")
    })
    MemberMonthlyReadingRankResponse getMemberMonthlyRank(@Parameter(hidden = true) Member member);
}
