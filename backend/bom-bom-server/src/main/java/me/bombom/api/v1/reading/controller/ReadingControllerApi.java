package me.bombom.api.v1.reading.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.reading.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.reading.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.reading.dto.response.WeeklyGoalCountResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Reading", description = "읽기 현황 관련 API")
public interface ReadingControllerApi {

    @Operation(
        summary = "주간 읽기 목표 업데이트",
        description = "사용자의 주간 읽기 목표량을 업데이트합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주간 목표 업데이트 성공",
            content = @Content(schema = @Schema(implementation = WeeklyGoalCountResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "로그인이 필요합니다"
        )
    })
    WeeklyGoalCountResponse updateWeeklyGoalCount(
        @Parameter(description = "주간 목표 업데이트 요청 데이터") @Valid @RequestBody UpdateWeeklyGoalCountRequest request
    );

    @Operation(
        summary = "읽기 지표 조회",
        description = "사용자의 읽기 지표 관련 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "읽기 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = ReadingInformationResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "로그인이 필요합니다"
        )
    })
    ReadingInformationResponse getReadingInformation(
        @Parameter(description = "로그인한 회원 정보") @LoginMember Member member
    );
} 
