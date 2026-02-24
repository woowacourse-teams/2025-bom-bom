package me.bombom.api.v1.coupon.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {@UniqueConstraint(columnNames = {"member_id", "coupon_name"})}
)
public class CouponIssue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    @Column(nullable = false, length = 100)
    private String couponName;

    @Column(nullable = false, length = 2048)
    private String imageUrl;

    @Builder
    public CouponIssue(
            Long id,
            Long memberId,
            @NonNull String couponName,
            @NonNull String imageUrl
    ) {
        this.id = id;
        this.memberId = memberId;
        this.couponName = couponName;
        this.imageUrl = imageUrl;
    }

    public static CouponIssue of(Long memberId, String couponName, String imageUrl) {
        return CouponIssue.builder()
                .memberId(memberId)
                .couponName(couponName)
                .imageUrl(imageUrl)
                .build();
    }
}
