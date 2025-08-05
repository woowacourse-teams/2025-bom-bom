package me.bombom.api.v1.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import me.bombom.api.v1.article.dto.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticleCategoryStatisticsResponse;
import me.bombom.api.v1.article.dto.GetArticlesOptions;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Article", description = "아티클 관련 API")
public interface ArticleControllerApi {

    @Operation(
            summary = "아티클 목록 조회",
            description = "조건에 맞는 아티클 목록을 페이징하여 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "아티클 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    public Page<ArticleResponse> getArticles(
            @Parameter(hidden = true) @LoginMember Member member,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)") @ModelAttribute GetArticlesOptions getArticlesOptions,
            @Parameter(description = "페이징 정보") @PageableDefault(sort = "arrivedDateTime", direction = Direction.DESC) Pageable pageable
    );

    @Operation(
        summary = "아티클 상세 조회",
        description = "특정 아티클의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "아티클 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = ArticleDetailResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 아티클 ID"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "아티클을 찾을 수 없음"
        )
    })
    @GetMapping("/{id}")
    ArticleDetailResponse getArticleDetail(
        @Parameter(description = "로그인한 회원 정보") @LoginMember Member member,
        @Parameter(description = "아티클 ID")
        @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    );

    @Operation(
        summary = "아티클 읽음 처리",
        description = "특정 아티클을 읽음 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "읽음 처리 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 아티클 ID"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "아티클을 찾을 수 없음"
        )
    })
//    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateIsRead(
        @Parameter(description = "로그인한 회원 정보") @LoginMember Member member,
        @Parameter(description = "아티클 ID", example = "1") 
        @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    );

    @Operation(
        summary = "카테고리별 아티클 통계 조회",
        description = "카테고리별 아티클 통계 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "카테고리별 통계 조회 성공",
            content = @Content(schema = @Schema(implementation = GetArticleCategoryStatisticsResponse.class))
        )
    })
    GetArticleCategoryStatisticsResponse getArticleCategoryStatistics(
        @Parameter(description = "로그인한 회원 정보") @LoginMember Member member,
        @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword
    );
}
