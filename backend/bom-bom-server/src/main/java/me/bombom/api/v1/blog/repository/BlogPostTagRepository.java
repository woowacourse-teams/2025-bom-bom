package me.bombom.api.v1.blog.repository;

import java.util.List;
import me.bombom.api.v1.blog.domain.BlogPostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlogPostTagRepository extends JpaRepository<BlogPostTag, Long> {

    @Query("""
        SELECT bh.name
        FROM BlogPostTag bpt
        JOIN BlogHashtag bh ON bh.id = bpt.blogHashtagId
        WHERE bpt.blogPostId = :blogPostId
    """)
    List<String> findHashtagNamesByBlogPostId(@Param("blogPostId") Long blogPostId);
}
