package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import me.bombom.api.v1.challenge.dto.request.CreateChallengeReviewRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Challenge Review", description = "챌린지 리뷰 관련 API")
public interface ChallengeReviewControllerApi {

    @Operation(
            summary = "열람 가능한 리뷰 목록 조회",
            description = "로그인한 사용자가 직접 작성한 리뷰(비공개 포함)와 다른 사용자가 작성한 공개 리뷰 목록을 함께 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음", content = @Content)
    })
    List<ChallengeReviewResponse> getReviews(
            @PathVariable @Positive(message = "challengeId는 1 이상의 값이어야 합니다.") Long challengeId,
            @Parameter(hidden = true) @LoginMember Member member
    );

    @Operation(
            summary = "리뷰 작성",
            description = "로그인한 사용자가 챌린지 리뷰를 작성합니다. 비공개 여부를 함께 지정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음", content = @Content)
    })
    void createReview(
            @PathVariable @Positive(message = "challengeId는 1 이상의 값이어야 합니다.") Long challengeId,
            @Valid @RequestBody CreateChallengeReviewRequest request,
            @Parameter(hidden = true) @LoginMember Member member
    );
}
