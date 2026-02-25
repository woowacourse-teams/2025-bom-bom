package me.bombom.api.v1.coupon.repository;

import java.util.List;
import me.bombom.api.v1.coupon.domain.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {

    List<CouponIssue> findByMemberId(Long id);
}
