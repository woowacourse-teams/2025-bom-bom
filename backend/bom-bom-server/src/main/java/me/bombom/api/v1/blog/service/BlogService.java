package me.bombom.api.v1.blog.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.domain.BlogCategory;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.domain.BlogPostVisibility;
import me.bombom.api.v1.blog.dto.response.BlogCategoryResponse;
import me.bombom.api.v1.blog.dto.response.BlogPostDetailResponse;
import me.bombom.api.v1.blog.dto.response.BlogPostResponse;
import me.bombom.api.v1.blog.repository.BlogCategoryRepository;
import me.bombom.api.v1.blog.repository.BlogImageAssetRepository;
import me.bombom.api.v1.blog.repository.BlogPostRepository;
import me.bombom.api.v1.blog.repository.BlogPostTagRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogService {

    private final BlogPostRepository blogPostRepository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final BlogImageAssetRepository blogImageAssetRepository;
    private final BlogPostTagRepository blogPostTagRepository;
    private final MemberRepository memberRepository;

    public Page<BlogPostResponse> getPublishedPosts(Member member, Pageable pageable) {
        Long memberId = member == null ? null : member.getId();
        return blogPostRepository.findPublishedPosts(memberId, pageable);
    }

    public BlogPostDetailResponse getPublishedPostDetail(Long postId, Member member) {
        Long memberId = member == null ? null : member.getId();
        BlogPost blogPost = findPublishedPostById(postId, memberId);
        validateAccessible(blogPost, memberId);

        String blogImageUrl = blogPost.getThumbnailImageId() == null ? null
                    : blogImageAssetRepository.findById(blogPost.getThumbnailImageId())
                            .map(BlogImageAsset::getImageUrl)
                            .orElse(null);

        BlogCategory blogCategory = blogCategoryRepository.findById(blogPost.getCategoryId())
                        .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                                .addContext(ErrorContextKeys.ENTITY_TYPE, "blogCategory")
                                .addContext("postId", postId)
                                .addContext("categoryId", blogPost.getCategoryId()));

        return BlogPostDetailResponse.of(
                blogPost,
                blogImageUrl,
                blogCategory,
                blogPostTagRepository.findHashtagNamesByBlogPostId(postId)
        );
    }

    public List<BlogCategoryResponse> getBlogCategories() {
        return blogCategoryRepository.findAllByOrderByIdAsc().stream()
                .map(BlogCategoryResponse::from)
                .toList();
    }

    private BlogPost findPublishedPostById(Long postId, Long memberId) {
        BlogPost blogPost = blogPostRepository.findById(postId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "blogPost")
                        .addContext("postId", postId)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId));

        if (blogPost.getStatus() != BlogPostStatus.PUBLISHED) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "blogPost")
                    .addContext("postId", postId)
                    .addContext("status", blogPost.getStatus());
        }
        return blogPost;
    }

    private void validateAccessible(BlogPost blogPost, Long memberId) {
        if (blogPost.getVisibility() == BlogPostVisibility.PUBLIC) {
            return;
        }

        boolean isAdmin = memberId != null && memberRepository.existsByIdAndRoleAuthority(memberId, "ADMIN");
        if (!isAdmin) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext("postId", blogPost.getId())
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.OPERATION, "getPublishedPostDetail");
        }
    }
}
