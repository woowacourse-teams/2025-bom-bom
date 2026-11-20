package me.bombom.api.v1.newsletterrequest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.newsletterrequest.dto.request.CreateNewsletterRequestRequest;
import me.bombom.api.v1.newsletterrequest.dto.response.CheckNewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.CreateNewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestLikeResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestSuggestionResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "NewsletterRequest", description = "뉴스레터 신청 관련 API")
public interface NewsletterRequestControllerApi {

    @Operation(
            summary = "뉴스레터 신청 현황 조회",
            description = "진행 중이거나 최근 30일 안에 등록된 신청을 공감 수 순서로 조회합니다. 로그인하면 내 좋아요 여부를 함께 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    List<NewsletterRequestResponse> getNewsletterRequests(@Parameter(hidden = true) Member member);

    @Operation(summary = "내 뉴스레터 신청 조회", description = "내가 신청했거나 좋아요한 신청을 최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    List<NewsletterRequestResponse> getMyNewsletterRequests(@Parameter(hidden = true) Member member);

    @Operation(
            summary = "뉴스레터 이름 추천",
            description = "이름 검색어로 진행 중인 신청과 등록된 뉴스레터를 최대 3개씩 조회합니다. 공백을 뺀 검색어가 2자 미만이면 빈 목록을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    NewsletterRequestSuggestionResponse getNewsletterRequestSuggestions(@Parameter(hidden = true) Member member, String keyword);

    @Operation(summary = "뉴스레터 신청 링크 중복 확인", description = "AVAILABLE, REQUESTED, REGISTERED 중 하나를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 성공"),
            @ApiResponse(responseCode = "400", description = "링크 형식 오류")
    })
    CheckNewsletterRequestResponse checkNewsletterRequest(String url);

    @Operation(summary = "뉴스레터 신청", description = "뉴스레터를 신청합니다. 신청 정보는 비동기로 자동 수집됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신청 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류 또는 이미 신청되었거나 등록된 링크"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    CreateNewsletterRequestResponse createNewsletterRequest(
            @Parameter(hidden = true) Member member,
            @Valid @RequestBody CreateNewsletterRequestRequest request
    );

    @Operation(summary = "뉴스레터 신청 좋아요", description = "진행 중인 신청에 좋아요를 누릅니다. 이미 눌렀다면 좋아요 수만 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 성공"),
            @ApiResponse(responseCode = "400", description = "진행 중이 아닌 신청이거나 내가 신청한 신청"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 신청")
    })
    NewsletterRequestLikeResponse addNewsletterRequestLike(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "뉴스레터 신청 ID") @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    );

    @Operation(summary = "뉴스레터 신청 좋아요 취소", description = "좋아요를 취소합니다. 누르지 않았다면 좋아요 수만 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 신청")
    })
    NewsletterRequestLikeResponse deleteNewsletterRequestLike(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "뉴스레터 신청 ID") @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id
    );
}
