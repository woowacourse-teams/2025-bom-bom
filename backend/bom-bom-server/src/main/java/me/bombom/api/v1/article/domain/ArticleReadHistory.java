package me.bombom.api.v1.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_article_read_history_member_article",
                columnNames = {"member_id", "article_id"}
        )
)
public class ArticleReadHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long articleId;

    @Column(nullable = false)
    private Long newsletterId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private LocalDateTime readAt;

    @Builder
    public ArticleReadHistory(
            Long id,
            @NonNull Long memberId,
            @NonNull Long articleId,
            @NonNull Long newsletterId,
            @NonNull Long categoryId,
            @NonNull LocalDateTime readAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.articleId = articleId;
        this.newsletterId = newsletterId;
        this.categoryId = categoryId;
        this.readAt = readAt;
    }
}
