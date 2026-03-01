package me.bombom.api.v1.newsletter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse;
import me.bombom.api.v1.newsletter.dto.NewslettersResponse;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Newsletter", description = "뉴스레터 관련 API")
public interface NewsletterControllerApi {

    @Operation(
            summary = "뉴스레터 목록 조회",
            description = """
                    카테고리 목록과 뉴스레터 목록을 함께 반환합니다.
                    - categories: 조회 조건에 부합하는 뉴스레터가 존재하는 카테고리 목록
                    - newsletters: 필터링된 뉴스레터 목록
                    - categoryId: 특정 카테고리로 필터링합니다. 미입력 시 전체 카테고리를 반환합니다.
                    - includeSuspended=false: 발행중(ACTIVE) 뉴스레터만 반환합니다.
                    - includeSuspended=true: 발행중(ACTIVE) 및 6개월 이내 휴재(SUSPENDED) 뉴스레터를 포함합니다.
                    - 6개월 초과 휴재(SUSPENDED)와 폐간(DISCONTINUED)은 항상 제외됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "뉴스레터 목록 조회 성공")
    })
    NewslettersResponse getNewsletterList(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "휴재 뉴스레터 포함 여부") @RequestParam(required = false, defaultValue = "false") boolean includeSuspended,
            @Parameter(description = "카테고리 ID (미입력 시 전체)") @RequestParam(required = false) Long categoryId
    );

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
            @Parameter(hidden = true) Long memberId,
            @Positive(message = "id는 1 이상의 값이어야 합니다.")
            @Parameter(description = "뉴스레터 ID") Long id
    );
}
