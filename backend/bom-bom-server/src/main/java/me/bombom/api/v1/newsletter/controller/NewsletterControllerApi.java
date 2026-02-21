package me.bombom.api.v1.newsletter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterWithDetailResponse;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Newsletter", description = "뉴스레터 관련 API")
public interface NewsletterControllerApi {

    @Operation(
            summary = "뉴스레터 목록 조회",
            description = "뉴스레터 목록을 조회합니다. 기본값은 발행중(ACTIVE)만 반환하며, includeSuspended=true 시 휴재(SUSPENDED)도 포함합니다. 폐간(DISCONTINUED)은 항상 제외됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "뉴스레터 목록 조회 성공")
    })
    List<NewsletterResponse> getNewsletters(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "휴재 뉴스레터 포함 여부") @RequestParam(required = false, defaultValue = "false") boolean includeSuspended
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
            @Parameter(hidden = true) Member member,
            @Positive(message = "id는 1 이상의 값이어야 합니다.")
            @Parameter(description = "뉴스레터 ID") Long id
    );
} 
