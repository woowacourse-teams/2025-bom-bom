package me.bombom.api.v1.highlight.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.highlight.dto.request.HighlightCreateRequest;
import me.bombom.api.v1.highlight.dto.request.UpdateHighlightRequest;
import me.bombom.api.v1.highlight.dto.response.HighlightResponse;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Highlight", description = "하이라이트 관련 API")
interface HighlightControllerApi {

    @Operation(
            summary = "하이라이트 목록 조회",
            description = "특정 아티클의 하이라이트 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "하이라이트 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 아티클 ID")
    })
    List<HighlightResponse> getHighlights(
            Member member,
            @Parameter(description = "아티클 ID") @RequestParam @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId
    );

    @Operation(
            summary = "하이라이트 생성",
            description = "새로운 하이라이트를 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "하이라이트 생성 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청 데이터")
    })
    void createHighlight(
            Member member,
            @Parameter(description = "하이라이트 생성 요청") @Valid @RequestBody HighlightCreateRequest createRequest
    );

    @Operation(
            summary = "하이라이트 삭제",
            description = "특정 하이라이트를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "하이라이트 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "하이라이트를 찾을 수 없음")
    })
    void deleteHighlight(
            Member member,
            @Parameter(description = "하이라이트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    );

    @Operation(
            summary = "하이라이트 색상 변경",
            description = "특정 하이라이트의 색상을 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "하이라이트 색상 변경 성공"),
            @ApiResponse(responseCode = "404", description = "하이라이트를 찾을 수 없음")
    })
    HighlightResponse updateHighlight(
            Member member,
            @Parameter(description = "하이라이트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Parameter(description = "색상 변경 요청") @Valid @RequestBody UpdateHighlightRequest request
    );
}
