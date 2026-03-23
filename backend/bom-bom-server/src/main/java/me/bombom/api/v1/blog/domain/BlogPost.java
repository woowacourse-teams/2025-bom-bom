package me.bombom.api.v1.blog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(
        indexes = {
                @Index(
                        name = "idx_blog_post_status_visibility_published_at",
                        columnList = "status, visibility, published_at"
                ),
                @Index(name = "idx_blog_post_category_id", columnList = "category_id")
        }
)
public class BlogPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "mediumtext")
    private String content;

    private Long thumbnailImageId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BlogPostStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BlogPostVisibility visibility;

    private Long categoryId;

    private Integer expectedReadTime;

    private LocalDateTime publishedAt;

    @Builder
    public BlogPost(
            Long id,
            @NonNull Long memberId,
            String title,
            String content,
            Long thumbnailImageId,
            @NonNull BlogPostStatus status,
            @NonNull BlogPostVisibility visibility,
            Long categoryId,
            Integer expectedReadTime,
            LocalDateTime publishedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.thumbnailImageId = thumbnailImageId;
        this.status = status;
        this.visibility = visibility;
        this.categoryId = categoryId;
        this.expectedReadTime = expectedReadTime;
        this.publishedAt = publishedAt;
    }
}
