package me.bombom.api.v1.subscribe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.subscribe.dto.UnsubscribeResponse;
import me.bombom.api.v1.subscribe.dto.SubscribedNewsletterResponse;

@Tag(name = "Subscription", description = "구독 관련 API")
public interface SubscribeControllerApi {

    @Operation(summary = "구독한 뉴스레터 목록 조회", description = "현재 로그인한 사용자가 구독한 뉴스레터 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "구독한 뉴스레터 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    List<SubscribedNewsletterResponse> getSubscribedNewsletters(Member member);

    @Operation(summary = "뉴스레터 구독 취소", description = "뉴스레터 구독을 취소합니다. 구독 리스트에서 삭제하고 unsubscribeUrl이 존재하는 경우 반환됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "구독 취소 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    UnsubscribeResponse unsubscribe(Member member, Long subscriptionId);
}
