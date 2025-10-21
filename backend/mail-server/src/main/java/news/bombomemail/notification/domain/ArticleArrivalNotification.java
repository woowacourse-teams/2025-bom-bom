package news.bombomemail.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import news.bombomemail.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleArrivalNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String newsletterName;

    @Column(nullable = false)
    private String articleTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int attempts = 0;

    private LocalDateTime nextRetryAt;

    @Column(length = 1024)
    private String lastError;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isRead = false;

    @Builder
    public ArticleArrivalNotification(
            Long id,
            @NonNull Long memberId,
            @NonNull String newsletterName,
            @NonNull String articleTitle,
            @NonNull NotificationStatus status,
            int attempts,
            LocalDateTime nextRetryAt,
            String lastError,
            boolean isRead
    ) {
        this.id = id;
        this.memberId = memberId;
        this.newsletterName = newsletterName;
        this.articleTitle = articleTitle;
        this.status = status;
        this.attempts = attempts;
        this.nextRetryAt = nextRetryAt;
        this.lastError = lastError;
        this.isRead = isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
