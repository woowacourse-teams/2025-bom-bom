package me.bombom.api.v1.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "article_arrival_notification")
public class ArticleArrivalNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long articleId;

    @Column(nullable = false)
    private String newsletterName;

    @Column(nullable = false, length = 500)
    private String articleTitle;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ArticleArrivalNotificationStatus status = ArticleArrivalNotificationStatus.PENDING;

    private int attempts;

    private LocalDateTime nextRetryAt;

    @Column(length = 1024)
    private String lastError;

    private boolean isRead;

    @Builder
    public ArticleArrivalNotification(
            Long id,
            @NonNull Long memberId,
            @NonNull Long articleId,
            @NonNull String newsletterName,
            @NonNull String articleTitle,
            ArticleArrivalNotificationStatus status,
            int attempts,
            LocalDateTime nextRetryAt,
            String lastError,
            boolean isRead
    ) {
        this.id = id;
        this.memberId = memberId;
        this.articleId = articleId;
        this.newsletterName = newsletterName;
        this.articleTitle = articleTitle;
        this.status = status != null ? status : ArticleArrivalNotificationStatus.PENDING;
        this.attempts = attempts;
        this.nextRetryAt = nextRetryAt;
        this.lastError = lastError;
        this.isRead = isRead;
    }
}
