package news.bombom.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleArrivalNotificationFailed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long originalNotificationId; // 추적용

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long articleId;

    @Column(nullable = false)
    private String newsletterName;

    @Column(nullable = false)
    private String articleTitle;

    private int finalAttempts;

    @Column(length = 1024)
    private String lastError;

    private LocalDateTime failedAt;

    @Builder
    public ArticleArrivalNotificationFailed(Long originalNotificationId, Long memberId, Long articleId,
                                            String newsletterName, String articleTitle, int finalAttempts,
                                            String lastError) {
        this.originalNotificationId = originalNotificationId;
        this.memberId = memberId;
        this.articleId = articleId;
        this.newsletterName = newsletterName;
        this.articleTitle = articleTitle;
        this.finalAttempts = finalAttempts;
        this.lastError = lastError;
        this.failedAt = LocalDateTime.now();
    }

    public static ArticleArrivalNotificationFailed from(ArticleArrivalNotification notification) {
        return ArticleArrivalNotificationFailed.builder()
                .originalNotificationId(notification.getId())
                .memberId(notification.getMemberId())
                .articleId(notification.getArticleId())
                .newsletterName(notification.getNewsletterName())
                .articleTitle(notification.getArticleTitle())
                .finalAttempts(notification.getAttempts())
                .lastError(notification.getLastError())
                .build();
    }
}
