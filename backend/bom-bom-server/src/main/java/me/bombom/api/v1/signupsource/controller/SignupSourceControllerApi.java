package me.bombom.api.v1.signupsource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.signupsource.dto.request.SaveSignupSourceRequest;
import me.bombom.api.v1.signupsource.dto.response.SignupSourceStatusResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "SignupSource", description = "가입 경로 질문 API")
public interface SignupSourceControllerApi {

    @Operation(
            summary = "가입 경로 질문 노출 여부 조회",
            description = "기능 배포 이후 가입했고 아직 가입 경로를 답하지 않았으면 needsSignupSource가 true입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음", content = @Content)
    })
    SignupSourceStatusResponse getSignupSourceStatus(@Parameter(hidden = true) Member member);

    @Operation(
            summary = "가입 경로 저장",
            description = "가입 경로를 저장합니다. 이미 저장했으면 아무것도 바꾸지 않습니다. 기타(ETC)는 detail이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 경로 값 또는 기타 입력 누락", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음", content = @Content)
    })
    void saveSignupSource(
            @Parameter(hidden = true) Member member,
            @Valid @RequestBody SaveSignupSourceRequest request
    );
}
