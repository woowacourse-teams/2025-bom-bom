package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubscribeRequest;

@Tag(name = "NativeNewsletter", description = "봄봄 자체 뉴스레터 구독 관련 API")
public interface MaeilMailSubscribeControllerApi {

    @Operation(summary = "봄봄 자체 뉴스레터 구독", description = "봄봄 자체 뉴스레터(매일메일)를 구독합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "구독 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (외부 뉴스레터, 중복 구독, 유효하지 않은 주간 발행 횟수)"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    void subscribe(
            @Parameter(hidden = true) @LoginMember Member member,
            @RequestBody(description = "weeklyIssueCount: 주간 발행 횟수 (1 또는 5)")
            @org.springframework.web.bind.annotation.RequestBody @Valid MaeilMailSubscribeRequest request
    );
}
