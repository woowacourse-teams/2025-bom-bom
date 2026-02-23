package me.bombom.api.v1.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import me.bombom.api.v1.coupon.dto.CouponIssueSummaryResponse;
import me.bombom.api.v1.member.domain.Member;

@Tag(name = "Coupon", description = "쿠폰 관련 API")
public interface CouponControllerApi {

    @Operation(
            summary = "내가 받은 쿠폰 목록 조회",
            description = "로그인한 사용자가 발급받은 쿠폰 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "쿠폰 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다.")
    })
    List<CouponIssueSummaryResponse> getIssuedCoupons(
            @Parameter(hidden = true) Member member
    );
}
