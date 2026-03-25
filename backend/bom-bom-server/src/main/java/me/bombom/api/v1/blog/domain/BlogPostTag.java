package me.bombom.api.v1.blog.domain;

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
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_blog_post_tag_post_tag",
                        columnNames = {"blog_post_id", "blog_hash_tag_id"}
                )
        }
)
public class BlogPostTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long blogPostId;

    @Column(nullable = false)
    private Long blogHashTagId;

    @Builder
    public BlogPostTag(
            Long id,
            @NonNull Long blogPostId,
            @NonNull Long blogHashTagId
    ) {
        this.id = id;
        this.blogPostId = blogPostId;
        this.blogHashTagId = blogHashTagId;
    }
}
