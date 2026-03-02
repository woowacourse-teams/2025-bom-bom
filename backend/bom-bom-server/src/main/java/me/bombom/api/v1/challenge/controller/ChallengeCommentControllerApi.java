package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentOptionsRequest;
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentRequest;
import me.bombom.api.v1.challenge.dto.request.UpdateChallengeCommentRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentCandidateArticleResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentHighlightResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentLikeResponse;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Challenge Comment", description = "챌린지 코멘트 관련 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content)
})
public interface ChallengeCommentControllerApi {

    @Operation(
            summary = "챌린지 팀 댓글 조회",
            description = "특정 챌린지에 참여 중인 사용자의 팀 댓글을 기간(start~end)으로 필터링해 페이징 조회합니다. "
                    + "(예: ?page=0&size=20&sort=createdAt,desc)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀 댓글 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "403", description = "팀 또는 챌린지 접근 권한 없음", content = @Content)
    })
    Page<ChallengeCommentResponse> getChallengeComments(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId,
            @Parameter(description = "필터링 관련 요청") @Valid @ModelAttribute ChallengeCommentOptionsRequest request,
            @Parameter(description = "페이징 및 정렬 (예: ?page=0&size=20&sort=createdAt,desc)") Pageable pageable
    );

    @Operation(
            summary = "챌린지 코멘트 후보 아티클 조회",
            description = "지정한 날짜에 읽은 아티클들을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "챌린지 코멘트 후보 아티클 조회 성공")
    })
    List<ChallengeCommentCandidateArticleResponse> getChallengeCommentCandidateArticles(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "대상 날짜 (예: 2025-12-31)") @RequestParam LocalDate date
    );

    @Operation(
            summary = "챌린지 코멘트 생성",
            description = "특정 챌린지에서 팀 코멘트를 작성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "챌린지 코멘트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "404", description = "아티클을 찾을 수 없음", content = @Content)
    })
    void createChallengeComment(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId,
            @Valid @RequestBody ChallengeCommentRequest request
    );

    @Operation(
            summary = "챌린지 아티클 하이라이트/메모 조회",
            description = "지정한 챌린지 아티클에 대해 내가 작성한 하이라이트/메모 목록을 페이징 조회합니다. "
                    + "(예: ?page=0&size=20&sort=createdAt,desc)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "하이라이트/메모 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
    })
    Page<ChallengeCommentHighlightResponse> getChallengeArticleHighlights(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "챌린지 아티클 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId,
            @Parameter(description = "페이징 및 정렬 (예: ?page=0&size=20&sort=createdAt,desc)") Pageable pageable
    );

    @Operation(
            summary = "챌린지 코멘트 수정",
            description = "챌린지 코멘트를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "챌린지 코멘트 수정 성공", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "403", description = "챌린지 코멘트 수정 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지 코멘트를 찾을 수 없음", content = @Content)
    })
    void updateChallengeComment(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "챌린지 id는 1 이상의 값이어야 합니다.") Long challengeId,
            @Parameter(description = "챌린지 코멘트 ID") @PathVariable @Positive(message = "코멘트 id는 1 이상의 값이어야 합니다.") Long commentId,
            @Valid @RequestBody UpdateChallengeCommentRequest request
    );

    @Operation(
            summary = "챌린지 코멘트 좋아요 추가",
            description = "특정 챌린지의 팀 코멘트에 좋아요를 추가하고 반영된 좋아요 개수를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "챌린지 코멘트 좋아요 추가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "403", description = "챌린지 코멘트 좋아요 추가 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지 코멘트를 찾을 수 없음", content = @Content)
    })
    ChallengeCommentLikeResponse addChallengeCommentLike(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "챌린지 id는 1 이상의 값이어야 합니다.") Long challengeId,
            @Parameter(description = "챌린지 코멘트 ID") @PathVariable @Positive(message = "코멘트 id는 1 이상의 값이어야 합니다.") Long commentId
    );

    @Operation(
            summary = "챌린지 코멘트 좋아요 취소",
            description = "특정 챌린지의 팀 코멘트에 좋아요를 취소하고 반영된 좋아요 개수를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "챌린지 코멘트 좋아요 취소 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "403", description = "챌린지 코멘트 좋아요 취소 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "챌린지 코멘트를 찾을 수 없음", content = @Content)
    })
    ChallengeCommentLikeResponse deleteChallengeCommentLike(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "챌린지 id는 1 이상의 값이어야 합니다.") Long challengeId,
            @Parameter(description = "챌린지 코멘트 ID") @PathVariable @Positive(message = "코멘트 id는 1 이상의 값이어야 합니다.") Long commentId
    );
}
