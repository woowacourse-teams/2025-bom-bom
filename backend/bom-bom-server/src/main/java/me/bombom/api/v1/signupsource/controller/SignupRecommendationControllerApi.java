package me.bombom.api.v1.signupsource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.signupsource.dto.response.SignupRecommendationsResponse;

@Tag(name = "SignupSource", description = "가입 직후 첫 구독 추천 API")
public interface SignupRecommendationControllerApi {

    @Operation(
            summary = "가입 직후 첫 구독 추천 목록 조회",
            description = "추천 순서대로 정렬한 뉴스레터와, 추천이 많은 카테고리 순서를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    SignupRecommendationsResponse getSignupRecommendations();
}
