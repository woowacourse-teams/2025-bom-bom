package me.bombom.api.v1.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "article_arrival_notification_failed")
public class ArticleArrivalNotificationFailed extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long originalNotificationId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long articleId;

    @Column(nullable = false)
    private String newsletterName;

    @Column(nullable = false)
    private String articleTitle;

    @Column(nullable = false)
    private int finalAttempts;

    @Column(length = 1024)
    private String lastError;

    @Column(nullable = false)
    private LocalDateTime failedAt;

    @Builder
    public ArticleArrivalNotificationFailed(
            Long id,
            @NonNull Long originalNotificationId,
            @NonNull Long memberId,
            @NonNull Long articleId,
            @NonNull String newsletterName,
            @NonNull String articleTitle,
            int finalAttempts,
            String lastError,
            @NonNull LocalDateTime failedAt
    ) {
        this.id = id;
        this.originalNotificationId = originalNotificationId;
        this.memberId = memberId;
        this.articleId = articleId;
        this.newsletterName = newsletterName;
        this.articleTitle = articleTitle;
        this.finalAttempts = finalAttempts;
        this.lastError = lastError;
        this.failedAt = failedAt;
    }
}
