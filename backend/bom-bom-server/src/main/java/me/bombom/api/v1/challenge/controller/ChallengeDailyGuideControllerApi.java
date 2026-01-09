package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.challenge.dto.request.DailyGuideCommentRequest;
import me.bombom.api.v1.challenge.dto.response.DailyGuideCommentResponse;
import me.bombom.api.v1.challenge.dto.response.TodayDailyGuideResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Challenge Daily Guide", description = "챌린지 데일리 가이드 관련 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content)
})
public interface ChallengeDailyGuideControllerApi {

    @Operation(
            summary = "오늘의 데일리 가이드 조회",
            description = "특정 챌린지의 오늘 날짜에 해당하는 데일리 가이드를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "오늘의 데일리 가이드 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 ID)", content = @Content),
            @ApiResponse(responseCode = "403", description = "챌린지 참여 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지 또는 데일리 가이드를 찾을 수 없음", content = @Content)
    })
    TodayDailyGuideResponse getTodayDailyGuide(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId
    );

    @Operation(
            summary = "데일리 가이드 코멘트 목록 조회",
            description = "특정 챌린지의 특정 일차 데일리 가이드에 작성된 코멘트 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "코멘트 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 ID)", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지 또는 데일리 가이드를 찾을 수 없음", content = @Content)
    })
    Page<DailyGuideCommentResponse> getDailyGuideComments(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId,
            @Parameter(description = "일차 인덱스 (1부터 시작)") @PathVariable @Positive(message = "index는 1 이상의 값이어야 합니다.") int dayIndex,
            @Parameter(description = "페이징 관련 요청 (예: ?page=0&size=20&sort=createdAt,desc)") Pageable pageable
    );

    @Operation(
            summary = "데일리 가이드 댓글 작성",
            description = "특정 챌린지의 특정 일차 데일리 가이드에 댓글을 작성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "댓글 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "403", description = "챌린지 참여 권한 없음 또는 댓글 작성 불가", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지 또는 데일리 가이드를 찾을 수 없음", content = @Content)
    })
    void createDailyGuideComment(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId,
            @Parameter(description = "일차 인덱스 (1부터 시작)") @PathVariable @Positive(message = "일차 인덱스는 1 이상의 값이어야 합니다.") int dayIndex,
            @Valid DailyGuideCommentRequest request
    );
}
