package me.bombom.api.v1.coupon.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.coupon.dto.response.CouponIssueResponse;
import me.bombom.api.v1.coupon.dto.response.CouponIssueSummaryResponse;
import me.bombom.api.v1.coupon.dto.response.CouponQueueStatusResponse;
import me.bombom.api.v1.coupon.service.CouponQueueService;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponQueueController implements CouponQueueControllerApi {

    private final CouponQueueService couponQueueService;

    @Override
    @PostMapping("/{couponName}/queue-entries")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponQueueStatusResponse registerQueue(
            @PathVariable String couponName,
            @LoginMember Member member
    ) {
        return couponQueueService.registerQueue(couponName, member);
    }

    @Override
    @GetMapping("/{couponName}/queue-entries/me")
    public CouponQueueStatusResponse getQueueStatus(
            @PathVariable String couponName,
            @LoginMember Member member
    ) {
        return couponQueueService.getQueueStatus(couponName, member);
    }

    @Override
    @DeleteMapping("/{couponName}/queue-entries/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveQueue(
            @PathVariable String couponName,
            @LoginMember Member member
    ) {
        couponQueueService.leaveQueue(couponName, member);
    }

    @Override
    @PostMapping("/{couponName}/issues")
    public CouponIssueResponse issueCoupon(
            @PathVariable String couponName,
            @LoginMember Member member
    ) {
        return couponQueueService.issueCoupon(couponName, member);
    }

    @Override
    @GetMapping("/issues/me")
    public List<CouponIssueSummaryResponse> getIssuedCoupons(@LoginMember Member member) {
        return couponQueueService.getIssuedCoupons(member);
    }
}
