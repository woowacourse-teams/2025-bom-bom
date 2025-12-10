package me.bombom.api.v1.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.article.dto.request.UpdateWarningSettingRequest;
import me.bombom.api.v1.article.dto.response.WarningSettingResponse;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Article", description = "아티클 최대 개수 임박 경고 관련 API")
public interface WarningControllerApi {

    @Operation(
            summary = "최대 개수 임박 경고 설정 조회",
            description = "500개 임박 알림 대해 '다시보지않기' 설정 여부를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경고 설정 조회 성공"),
            @ApiResponse(responseCode = "404", description = "경고 설정 상태를 찾을 수 없음", content = @Content)
    })
    WarningSettingResponse getCapacityWarningStatus(@Parameter(hidden = true) Member member);

    @Operation(
            summary = "최대 개수 임박 경고 설정 변경",
            description = "500개 임박 알림에 대해 '다시보지않기' 설정 여부를 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경고 설정 변경 성공"),
            @ApiResponse(responseCode = "404", description = "경고 설정 상태를 찾을 수 없음", content = @Content)
    })
    void updateWarningSetting(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "경고 설정 변경 요청") @Valid @RequestBody UpdateWarningSettingRequest request
    );
}
