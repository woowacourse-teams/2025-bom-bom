package me.bombom.api.v1.coupon.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.coupon.dto.CouponIssueSummaryResponse;
import me.bombom.api.v1.coupon.repository.CouponIssueRepository;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponIssueRepository couponIssueRepository;

    public List<CouponIssueSummaryResponse> getIssuedCoupons(Member member) {
        return couponIssueRepository.findByMemberId(member.getId())
                .stream()
                .map(issue -> CouponIssueSummaryResponse.of(
                        issue.getCouponName(),
                        issue.getImageUrl(),
                        issue.getUpdatedAt())
                )
                .toList();
    }
}
