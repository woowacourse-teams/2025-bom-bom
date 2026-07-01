package me.bombom.api.v1.coupon.repository;

import java.util.List;
import me.bombom.api.v1.coupon.domain.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {

    List<CouponIssue> findByMemberId(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM CouponIssue c
            WHERE c.memberId = :memberId
            """)
    void bulkDeleteAllByMemberId(Long memberId);
}
