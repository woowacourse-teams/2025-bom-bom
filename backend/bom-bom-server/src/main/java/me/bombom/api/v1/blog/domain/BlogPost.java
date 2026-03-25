package me.bombom.api.v1.blog.domain;

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
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(length = 500)
    private String description;

    private Long thumbnailImageId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BlogPostStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BlogPostVisibility visibility;

    private Long categoryId;

    private LocalDateTime publishedAt;

    @Builder
    public BlogPost(
            Long id,
            @NonNull Long memberId,
            String title,
            String content,
            String description,
            Long thumbnailImageId,
            @NonNull BlogPostStatus status,
            @NonNull BlogPostVisibility visibility,
            Long categoryId,
            LocalDateTime publishedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.description = description;
        this.thumbnailImageId = thumbnailImageId;
        this.status = status;
        this.visibility = visibility;
        this.categoryId = categoryId;
        this.publishedAt = publishedAt;
    }
}
