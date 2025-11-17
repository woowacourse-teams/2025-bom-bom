package me.bombom.api.v1.newsletter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "newsletter_previous_policy",
        uniqueConstraints = @UniqueConstraint(name = "uk_newsletter_previous_article_policy_newsletter_id", columnNames = "newsletter_id")
)
public class NewsletterPreviousPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long newsletterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NewsletterPreviousStrategy strategy;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int totalCount;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int fixedCount;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private int exposureRatio;

    @Builder
    public NewsletterPreviousPolicy(
            Long id,
            @NonNull Long newsletterId,
            @NonNull NewsletterPreviousStrategy strategy,
            int totalCount,
            int fixedCount,
            int exposureRatio
    ) {
        this.id = id;
        this.newsletterId = newsletterId;
        this.strategy = strategy;
        this.totalCount = totalCount;
        this.fixedCount = fixedCount;
        this.exposureRatio = exposureRatio;
    }
}


