package me.bombom.api.v1.signupsource.domain;

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

/**
 * 가입 직후 첫 구독으로 권하는 뉴스레터와 그 순서.
 * 순서와 도착 요일은 BigQuery 읽음 데이터로 정해 마이그레이션으로 넣는다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_signup_recommendation_newsletter_id", columnNames = "newsletter_id")
})
public class SignupRecommendation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long newsletterId;

    @Column(nullable = false)
    private int displayOrder;

    @Column(length = 30)
    private String arrivalDays;

    @Builder
    public SignupRecommendation(
            Long id,
            @NonNull Long newsletterId,
            int displayOrder,
            String arrivalDays
    ) {
        this.id = id;
        this.newsletterId = newsletterId;
        this.displayOrder = displayOrder;
        this.arrivalDays = arrivalDays;
    }
}
