package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;

@Tag(name = "Challenge Review", description = "챌린지 리뷰 관련 API")
public interface ChallengeReviewControllerApi {

    @Operation(
            summary = "열람 가능한 리뷰 목록 조회",
            description = "로그인한 사용자가 직접 작성한 리뷰(비공개 포함)와 다른 사용자가 작성한 공개 리뷰 목록을 함께 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content)
    })
    List<ChallengeReviewResponse> getReviews(
            @Parameter(hidden = true) @LoginMember Member member
    );
}
