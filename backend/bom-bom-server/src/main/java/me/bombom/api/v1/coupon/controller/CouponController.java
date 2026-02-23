package me.bombom.api.v1.coupon.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.coupon.dto.CouponIssueSummaryResponse;
import me.bombom.api.v1.coupon.service.CouponService;
import me.bombom.api.v1.member.domain.Member;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupon")
public class CouponController implements CouponControllerApi {

    private final CouponService couponService;

    @Override
    @GetMapping("issues/me")
    public List<CouponIssueSummaryResponse> getIssuedCoupons(@LoginMember Member member) {
        return couponService.getIssuedCoupons(member);
    }
}
