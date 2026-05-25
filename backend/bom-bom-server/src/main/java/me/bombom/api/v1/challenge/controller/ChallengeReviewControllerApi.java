package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.challenge.dto.request.CreateChallengeReviewRequest;
import me.bombom.api.v1.challenge.dto.request.UpdateChallengeReviewRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeReviewResponse;
import me.bombom.api.v1.challenge.dto.response.MyChallengeReviewResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Challenge Review", description = "챌린지 리뷰 관련 API")
public interface ChallengeReviewControllerApi {

    @Operation(
            summary = "열람 가능한 리뷰 목록 조회",
            description = "로그인한 사용자가 직접 작성한 리뷰(비공개 포함)와 다른 사용자가 작성한 공개 리뷰 목록을 함께 조회합니다. "
                    + "응답은 `Page<ChallengeReviewResponse>` 형식이며, 정렬은 항상 최신순으로 서버가 강제합니다. "
                    + "`page`, `size` 쿼리 파라미터만 사용 가능하며, `sort` 파라미터는 무시됩니다. "
                    + "각 항목의 `isMyReview` 필드로 로그인 회원 본인 작성 여부를 분기 처리할 수 있습니다. "
                    + "(v2: 응답에 `isMyReview` 추가, 페이징 적용)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음", content = @Content)
    })
    Page<ChallengeReviewResponse> getReviews(
            @PathVariable @Positive(message = "challengeId는 1 이상의 값이어야 합니다.") Long challengeId,
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(description = "페이징 요청 (예: ?page=0&size=20). 정렬은 항상 최신순으로 서버 강제이며 sort 파라미터는 무시됩니다.") Pageable pageable
    );

    @Operation(
            summary = "내 리뷰 존재 확인",
            description = "로그인한 사용자가 해당 챌린지에 이미 작성한 리뷰가 있는지 확인합니다. "
                    + "리뷰가 존재하면 200과 함께 리뷰 본문을 반환하고, 없으면 404를 반환합니다. "
                    + "프론트엔드는 이 응답으로 리뷰 작성 버튼 활성화 여부를 결정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 리뷰가 존재함"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 챌린지에 작성한 리뷰가 없음", content = @Content)
    })
    MyChallengeReviewResponse getMyReview(
            @PathVariable @Positive(message = "challengeId는 1 이상의 값이어야 합니다.") Long challengeId,
            @Parameter(hidden = true) @LoginMember Member member
    );

    @Operation(
            summary = "리뷰 작성",
            description = "로그인한 사용자가 챌린지 리뷰를 작성합니다. 비공개 여부를 함께 지정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효성 검증 실패, 또는 해당 챌린지에 이미 작성한 리뷰가 존재)",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음", content = @Content)
    })
    void createReview(
            @PathVariable @Positive(message = "challengeId는 1 이상의 값이어야 합니다.") Long challengeId,
            @Valid @RequestBody CreateChallengeReviewRequest request,
            @Parameter(hidden = true) @LoginMember Member member
    );

    @Operation(
            summary = "리뷰 수정",
            description = "로그인한 사용자가 자신의 챌린지 리뷰를 수정합니다. 코멘트와 비공개 여부를 함께 수정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "리뷰 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
            @ApiResponse(responseCode = "403", description = "본인이 작성하지 않은 리뷰는 수정할 수 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지 또는 리뷰를 찾을 수 없음", content = @Content)
    })
    void updateReview(
            @PathVariable @Positive(message = "challengeId는 1 이상의 값이어야 합니다.") Long challengeId,
            @PathVariable @Positive(message = "reviewId는 1 이상의 값이어야 합니다.") Long reviewId,
            @Valid @RequestBody UpdateChallengeReviewRequest request,
            @Parameter(hidden = true) @LoginMember Member member
    );
}
