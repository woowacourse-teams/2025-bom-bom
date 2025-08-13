package me.bombom.api.v1.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.article.dto.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticleNewsletterStatisticsResponse;
import me.bombom.api.v1.article.dto.GetArticlesOptions;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Article", description = "아티클 관련 API")
public interface ArticleControllerApi {

    @Operation(
            summary = "아티클 목록 조회",
            description = "조건에 맞는 아티클 목록을 페이징하여 조회합니다. "
                    + "(정렬 기본값: ?page=0&size=10&sort=arrivedDateTime,desc)"

    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "아티클 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 정렬 파라미터 요청", content = @Content)
    })
    public Page<ArticleResponse> getArticles(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "필터링 관련 요청") @ModelAttribute GetArticlesOptions getArticlesOptions,
            @Parameter(description = "페이징 관련 요청 (예: ?page=0&size=10&sort=createdAt,desc)") Pageable pageable
    );

    @Operation(
        summary = "아티클 상세 조회",
        description = "특정 아티클의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "아티클 상세 조회 성공"),
        @ApiResponse(responseCode = "403", description = "아티클에 대한 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "아티클을 찾을 수 없음", content = @Content)
    })
    ArticleDetailResponse getArticleDetail(
        @Parameter(hidden = true) Member member,
        @Parameter(description = "아티클 ID")
        @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    );

    @Operation(
        summary = "아티클 읽음 처리",
        description = "특정 아티클을 읽음 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "읽음 처리 성공"),
        @ApiResponse(responseCode = "403", description = "아티클에 대한 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "아티클을 찾을 수 없음", content = @Content)
    })
    void updateIsRead(
        @Parameter(hidden = true) Member member,
        @Parameter(description = "아티클 ID", example = "1")
        @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    );

    @Operation(
        summary = "뉴스레터별 아티클 개수 조회",
        description = "뉴스레터별 아티클 개수 정보를 조회합니다. 키워드 검색 시 해당 키워드가 제목에 포함된 아티클만 대상으로 합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "뉴스레터별 개수 조회 성공")
    })
    GetArticleNewsletterStatisticsResponse getArticleNewsletterStatistics(
        @Parameter(hidden = true) Member member,
        @Parameter(description = "검색 키워드 (선택)") @RequestParam(required = false) String keyword
    );
}
