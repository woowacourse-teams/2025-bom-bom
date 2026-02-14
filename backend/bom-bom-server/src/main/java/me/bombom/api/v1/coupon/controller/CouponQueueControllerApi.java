package me.bombom.api.v1.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import me.bombom.api.v1.common.exception.ErrorResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.coupon.dto.response.CouponIssueResponse;
import me.bombom.api.v1.coupon.dto.response.CouponIssueSummaryResponse;
import me.bombom.api.v1.coupon.dto.response.CouponQueueStatusResponse;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Coupon", description = "선착순 쿠폰 대기열 API")
@RequestMapping("/api/v1/coupons")
public interface CouponQueueControllerApi {

    @Operation(
            summary = "선착순 쿠폰 대기열 등록",
            description = "쿠폰 이벤트에 대한 선착순 대기열에 현재 사용자를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "대기열 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (존재하지 않는 쿠폰, 중복 신청 등)", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{couponName}/queue-entries")
    CouponQueueStatusResponse registerQueue(
            @PathVariable @NotBlank(message = "couponName은 비어 있을 수 없습니다.") String couponName,
            @Parameter(hidden = true) @LoginMember Member member
    );

    @Operation(
            summary = "선착순 쿠폰 대기열 상태 조회",
            description = "현재 사용자의 대기열 순번/상태 및 남은 수를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (존재하지 않는 쿠폰)", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{couponName}/queue-entries/me")
    CouponQueueStatusResponse getQueueStatus(
            @PathVariable @NotBlank(message = "couponName은 비어 있을 수 없습니다.") String couponName,
            @Parameter(hidden = true) @LoginMember Member member
    );

    @Operation(
            summary = "선착순 쿠폰 발급",
            description = "입장 허용 상태(active)인 사용자만 쿠폰을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발급 확정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (입장 허용 상태 아님 등)", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{couponName}/issues")
    CouponIssueResponse issueCoupon(
            @PathVariable @NotBlank(message = "couponName은 비어 있을 수 없습니다.") String couponName,
            @Parameter(hidden = true) @LoginMember Member member
    );

    @Operation(
            summary = "내 쿠폰 발급 내역 조회",
            description = "현재 사용자의 발급된 쿠폰 목록을 최신순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/issues/me")
    List<CouponIssueSummaryResponse> getIssuedCoupons(
            @Parameter(hidden = true) @LoginMember Member member
    );
}
