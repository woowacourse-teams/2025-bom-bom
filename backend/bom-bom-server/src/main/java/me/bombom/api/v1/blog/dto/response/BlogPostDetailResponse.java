package me.bombom.api.v1.blog.dto.response;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.blog.domain.BlogCategory;
import me.bombom.api.v1.blog.domain.BlogPost;

public record BlogPostDetailResponse(

        @NotNull
        String title,

        @NotNull
        String content,

        String thumbnailImageUrl,

        @NotNull
        String categoryName,

        Integer expectedReadTime,

        @NotNull
        LocalDateTime publishedAt,

        @NotNull
        List<String> hashTags
) {

    public static BlogPostDetailResponse of(
            BlogPost blogPost,
            String thumbnailImageUrl,
            BlogCategory category,
            List<String> hashTags
    ) {
        return new BlogPostDetailResponse(
                blogPost.getTitle(),
                blogPost.getContent(),
                thumbnailImageUrl,
                category.getName(),
                blogPost.getExpectedReadTime(),
                blogPost.getPublishedAt(),
                hashTags
        );
    }
}
