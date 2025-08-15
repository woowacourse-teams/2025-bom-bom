package me.bombom.api.v1.newsletter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse;

import java.util.List;

@Tag(name = "Newsletter", description = "뉴스레터 관련 API")
public interface NewsletterControllerApi {

    @Operation(
        summary = "뉴스레터 목록 조회",
        description = "모든 뉴스레터 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "뉴스레터 목록 조회 성공")
    })
    List<NewsletterResponse> getNewsletters();

    @Operation(
        summary = "뉴스레터 상세 조회",
        description = "특정 뉴스레터 상세 조회를 합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "뉴스레터 상세 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 ID)", content = @Content),
        @ApiResponse(responseCode = "404", description = "뉴스레터를 찾을 수 없음", content = @Content)
    })
    NewsletterWithDetailResponse getNewsletterWithDetail(
            @Parameter(description = "뉴스레터 ID") Long id
    );
} 
