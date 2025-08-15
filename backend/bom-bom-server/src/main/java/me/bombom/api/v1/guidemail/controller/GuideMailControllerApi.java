package me.bombom.api.v1.guidemail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.member.domain.Member;

@Tag(name = "GuideMail", description = "가이드 메일 관련 API")
public interface GuideMailControllerApi {

    @Operation(
        summary = "가이드 메일 읽음 처리",
        description = "가이드 메일 읽었을 시, 키우기/읽기 점수 증가"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "읽음 처리 성공"),
        @ApiResponse(responseCode = "404", description = "멤버의 읽기 정보가 없을 경우")
    })
    void updateRead(
        @Parameter(hidden = true) Member member
    );
}
