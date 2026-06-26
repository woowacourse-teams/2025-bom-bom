package me.bombom.api.v1.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.response.CategoryStatsResponse;
import me.bombom.api.v1.member.dto.response.RankSummaryResponse;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "MyPage", description = "마이페이지 관련 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content)
})
public interface MyPageControllerApi {

    @Operation(
            summary = "마이페이지 랭킹 요약 조회",
            description = """
                    로그인한 회원의 마이페이지 랭킹 요약 정보를 조회합니다.
                    - type 미입력: 연속 읽기 랭킹과 읽은 글 수 랭킹을 모두 반환합니다.
                    - type=streak: 연속 읽기 랭킹만 반환합니다.
                    - type=reading: 읽은 글 수 랭킹만 반환합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "마이페이지 랭킹 요약 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 랭킹 타입", content = @Content)
    })
    RankSummaryResponse getRankSummary(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(
                    description = "랭킹 타입 (미입력 시 전체)",
                    schema = @Schema(allowableValues = {"streak", "reading"})
            )
            @RequestParam(required = false) String type
    );

    @Operation(
            summary = "마이페이지 월별 카테고리 통계 조회",
            description = "로그인한 회원이 지정한 월에 읽은 뉴스의 카테고리별 통계를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "마이페이지 월별 카테고리 통계 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 yearMonth 형식", content = @Content)
    })
    CategoryStatsResponse getCategoryStats(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(
                    description = "조회할 연월",
                    example = "2026-05",
                    schema = @Schema(pattern = "^\\d{4}-\\d{2}$")
            )
            @RequestParam(required = false) String yearMonth
    );
}
