package me.bombom.api.v1.highlight.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Highlight", description = "하이라이트 관련 API")
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content)
})
public interface HighlightControllerApi {

    @Operation(summary = "하이라이트 목록 조회", description = "조건에 맞는 하이라이트 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "하이라이트 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
    })
    List<HighlightResponse> getHighlights(
        @Parameter(hidden = true) Member member,
        @Parameter(description = "아티클 ID (예: ?articleId=1)") @RequestParam(required = false) @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId,
        @Parameter(description = "뉴스레터 ID (예: ?newsletterId=1)") @RequestParam(required = false) @Positive(message = "id는 1 이상의 값이어야 합니다.") Long newsletterId
    );

    @Operation(summary = "하이라이트 생성", description = "새로운 하이라이트를 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "하이라이트 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
        @ApiResponse(responseCode = "403", description = "아티클에 대한 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "아티클을 찾을 수 없음", content = @Content)
    })
    HighlightResponse createHighlight(
        @Parameter(hidden = true) Member member,
        @Valid @RequestBody HighlightCreateRequest request
    );

    @Operation(summary = "하이라이트 내용/위치 수정", description = "특정 하이라이트의 내용(텍스트)이나 위치를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "하이라이트 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content),
        @ApiResponse(responseCode = "403", description = "하이라이트에 대한 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "하이라이트를 찾을 수 없음", content = @Content)
    })
    HighlightResponse updateHighlight(
        @Parameter(hidden = true) @LoginMember Member member,
        @Parameter(description = "하이라이트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
        @Valid @RequestBody UpdateHighlightRequest request
    );

    @Operation(summary = "하이라이트 삭제", description = "특정 하이라이트를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "하이라이트 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "하이라이트에 대한 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "하이라이트를 찾을 수 없음", content = @Content)
    })
    void deleteHighlight(
        @Parameter(hidden = true) Member member,
        @Parameter(description = "하이라이트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long highlightId
    );
}
