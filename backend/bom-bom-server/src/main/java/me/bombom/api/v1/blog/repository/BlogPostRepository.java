package me.bombom.api.v1.blog.repository;

import java.util.Optional;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.dto.response.BlogPostResponse;
import me.bombom.api.v1.blog.dto.response.BlogPostSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    @Query(
            value = """
            SELECT new me.bombom.api.v1.blog.dto.response.BlogPostResponse(
                bp.id,
                bp.title,
                bp.description,
                bia.imageUrl,
                bc.name,
                bp.publishedAt
            )
            FROM BlogPost bp
            LEFT JOIN BlogImageAsset bia ON bia.id = bp.thumbnailImageId
            LEFT JOIN BlogCategory bc ON bc.id = bp.categoryId
            WHERE bp.status = me.bombom.api.v1.blog.domain.BlogPostStatus.PUBLISHED
                AND (
                    bp.visibility = me.bombom.api.v1.blog.domain.BlogPostVisibility.PUBLIC
                    OR (
                        bp.visibility = me.bombom.api.v1.blog.domain.BlogPostVisibility.PRIVATE
                        AND EXISTS (
                            SELECT 1
                            FROM Member m
                            JOIN Role r ON r.id = m.roleId
                            WHERE m.id = :memberId
                              AND r.authority = 'ADMIN'
                        )
                    )
                )
            """,
            countQuery = """
            SELECT COUNT(bp)
            FROM BlogPost bp
            WHERE bp.status = me.bombom.api.v1.blog.domain.BlogPostStatus.PUBLISHED
                AND (
                    bp.visibility = me.bombom.api.v1.blog.domain.BlogPostVisibility.PUBLIC
                    OR (
                        bp.visibility = me.bombom.api.v1.blog.domain.BlogPostVisibility.PRIVATE
                        AND EXISTS (
                            SELECT 1
                            FROM Member m
                            JOIN Role r ON r.id = m.roleId
                            WHERE m.id = :memberId
                              AND r.authority = 'ADMIN'
                        )
                    )
                )
            """
    )
    Page<BlogPostResponse> findPublishedPosts(@Param("memberId") Long memberId, Pageable pageable);

    @Query("""
            SELECT new me.bombom.api.v1.blog.dto.response.BlogPostSummaryResponse(
                bp.id,
                bp.title,
                bp.description,
                bia.imageUrl,
                bc.name,
                bp.publishedAt
            )
            FROM BlogPost bp
            LEFT JOIN BlogImageAsset bia ON bia.id = bp.thumbnailImageId
            LEFT JOIN BlogCategory bc ON bc.id = bp.categoryId
            WHERE bp.status = me.bombom.api.v1.blog.domain.BlogPostStatus.PUBLISHED
                AND bp.id = :postId
            """)
    Optional<BlogPostSummaryResponse> findPublishedPostSummaryById(@Param("postId") Long postId);
}
