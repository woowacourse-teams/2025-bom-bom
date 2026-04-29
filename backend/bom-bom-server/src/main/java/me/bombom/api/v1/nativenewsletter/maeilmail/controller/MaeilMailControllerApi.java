package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailIdealAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailInformationResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubmitAnswerRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubmittedAnswerResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "MaeilMail", description = "매일메일 서비스 관련 API")
public interface MaeilMailControllerApi {

    @Operation(summary = "매일메일 모범 답변 조회", description = "매일메일 컨텐츠의 모범 답변을 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 매일메일 컨텐츠", content = @Content),
    })
    MaeilMailIdealAnswerResponse getIdealAnswer(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "매일메일 컨텐츠 id") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long contentId
    );

    @Operation(summary = "매일메일 답변 제출", description = "회원이 매일메일 아티클에 대한 답변을 제출합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "너무 긴 답변 제출 시 (최대 16,000자)", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 매일메일 아티클", content = @Content),
    })
    void submitAnswer(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "매일메일 아티클 id") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId,
            @RequestBody @Valid MaeilMailSubmitAnswerRequest request
    );

    @Operation(summary = "매일메일 제출 답변 조회", description = "회원이 제출한 매일메일 아티클 답변을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 답변", content = @Content),
    })
    MaeilMailSubmittedAnswerResponse getSubmittedAnswer(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "매일메일 아티클 id") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId
    );

    @Operation(summary = "매일메일 정보 조회", description = "아티클 id로 매일메일 컨텐츠 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 아티클", content = @Content),
    })
    MaeilMailInformationResponse getInformationByArticle(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "매일메일 아티클 id") @RequestParam @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId
    );
}
