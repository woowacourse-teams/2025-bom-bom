package me.bombom.api.v1.reading.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.reading.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.reading.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.reading.dto.response.WeeklyGoalCountResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Reading", description = "읽기 정보 관련 API")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content)
})
public interface ReadingControllerApi {

    @Operation(summary = "읽기 정보 조회", description = "현재 사용자의 주간/오늘/연속 읽기 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "읽기 정보 조회 성공")
    })
    ResponseEntity<ReadingInformationResponse> getReadingInformation(
        @Parameter(hidden = true) Member member
    );

    @Operation(summary = "주간 목표 아티클 개수 수정", description = "주간 목표로 설정된 아티클 개수를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주간 목표 개수 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
        @ApiResponse(responseCode = "404", description = "주간 읽기 정보를 찾을 수 없음", content = @Content)
    })
    ResponseEntity<WeeklyGoalCountResponse> updateWeeklyGoalCount(
        @Parameter(hidden = true) Member member,
        @Valid @RequestBody UpdateWeeklyGoalCountRequest request
    );
} 
