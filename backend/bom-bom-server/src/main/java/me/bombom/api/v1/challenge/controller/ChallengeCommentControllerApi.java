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
import me.bombom.api.v1.challenge.dto.request.ChallengeCommentOptionsRequest;
import me.bombom.api.v1.challenge.dto.response.ChallengeCommentResponse;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
            @Parameter(hidden = true) Member member,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId,
            @Parameter(description = "필터링 관련 요청") @Valid @ModelAttribute ChallengeCommentOptionsRequest request,
            @Parameter(description = "페이징 및 정렬 (예: ?page=0&size=20&sort=createdAt,desc)") Pageable pageable
    );
}
