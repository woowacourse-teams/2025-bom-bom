package me.bombom.api.v1.coupon.repository;

import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.coupon.domain.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponIssueRepository extends JpaRepository<CouponIssue, Long> {
    boolean existsByMemberIdAndCouponName(Long memberId, String couponName);

    List<CouponIssue> findByMemberIdOrderByUpdatedAtDesc(Long memberId);

    long countByCouponNameAndMemberIdIsNotNull(String couponName);

    @Query(value = """
            SELECT
                SUM(CASE WHEN member_id IS NULL THEN 1 ELSE 0 END) AS availableCount,
                SUM(CASE WHEN member_id IS NOT NULL THEN 1 ELSE 0 END) AS issuedCount
            FROM coupon_issue
            WHERE coupon_name = :couponName
            """, nativeQuery = true)
    CouponIssueStockCount getStockCountByCouponName(@Param("couponName") String couponName);

    interface CouponIssueStockCount {
        Long getAvailableCount();
        Long getIssuedCount();
    }

    @Modifying
    @Query(value = """
            WITH selected_issue AS (
                SELECT id
                FROM coupon_issue
                WHERE coupon_name = :couponName
                  AND member_id IS NULL
                ORDER BY id
                LIMIT 1
                FOR UPDATE SKIP LOCKED
            )
            UPDATE coupon_issue ci
            JOIN selected_issue si ON si.id = ci.id
            SET ci.member_id = :memberId,
                ci.updated_at = NOW()
            """, nativeQuery = true)
    int assignAvailableIssueToMember(@Param("couponName") String couponName, @Param("memberId") Long memberId);

    Optional<CouponIssue> findTopByMemberIdAndCouponNameOrderByUpdatedAtDesc(Long memberId, String couponName);
}
