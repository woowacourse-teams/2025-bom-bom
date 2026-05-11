package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubscriptionResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailUpdateSubscriptionRequest;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "NativeNewsletter", description = "봄봄 자체 뉴스레터 구독 관련 API")
public interface MaeilMailSubscribeControllerApi {

    @Operation(summary = "봄봄 자체 뉴스레터 구독 여부 조회", description = "매일메일 구독 여부와 구독 중인 트랙 목록을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    MaeilMailSubscriptionResponse getSubscription(
            @Parameter(hidden = true) @LoginMember Member member
    );

    @Operation(
            summary = "봄봄 자체 뉴스레터 구독 생성/수정/해지",
            description = "요청한 트랙 목록으로 구독 상태를 치환합니다. "
                    + "미구독 상태에서 트랙을 보내면 신규 구독, 구독 중에 다른 트랙을 보내면 수정, "
                    + "빈 배열을 보내면 구독이 완전히 해지됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "처리 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (중복 트랙)"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    void putSubscription(
            @Parameter(hidden = true) @LoginMember Member member,
            @RequestBody @Valid MaeilMailUpdateSubscriptionRequest request
    );
}
