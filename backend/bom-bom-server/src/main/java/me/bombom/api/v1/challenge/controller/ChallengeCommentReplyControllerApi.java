package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.challenge.dto.request.CreateCommentReplyRequest;
import me.bombom.api.v1.challenge.dto.response.CommentReplyResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Challenge Comment Reply", description = "챌린지 코멘트 답글 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content)
})
public interface ChallengeCommentReplyControllerApi {

    @Operation(
            summary = "코멘트 답글 생성",
            description = """
                    특정 코멘트에 대해 답글을 작성합니다.
                    
                    - isPrivate=true: 비공개 답글 (코멘트 작성자 및 본인에게만 공개)
                    - isPrivate=false: 공개 답글 (모든 챌린지 참여자에게 공개)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "코멘트 답글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "403", description = "코멘트 답글 작성 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "코멘트를 찾을 수 없음", content = @Content)
    })
    void createCommentReply(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId,
            @Parameter(description = "코멘트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long commentId,
            @Valid @RequestBody CreateCommentReplyRequest request
    );

    @Operation(
            summary = "코멘트 답글 조회",
            description = "특정 코멘트에 달린 답글 목록을 페이지네이션하여 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "답글 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
            @ApiResponse(responseCode = "403", description = "답글 조회 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "코멘트를 찾을 수 없음", content = @Content)
    })
    Page<CommentReplyResponse> getCommentReplies(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(description = "챌린지 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId,
            @Parameter(description = "코멘트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long commentId,
            @Parameter(description = "페이지/정렬 정보 (page, size, sort)") Pageable pageable
    );
}
