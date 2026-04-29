package me.bombom.api.v1.blog.controller;

import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.dto.response.BlogCategoryResponse;
import me.bombom.api.v1.blog.dto.response.BlogPostDetailResponse;
import me.bombom.api.v1.blog.dto.response.BlogPostResponse;
import me.bombom.api.v1.blog.dto.response.BlogPostSummaryResponse;
import me.bombom.api.v1.blog.service.BlogService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/blog")
public class BlogPostController implements BlogPostControllerApi {

    private final BlogService blogService;

    @Override
    @GetMapping("/posts")
    public Page<BlogPostResponse> getPublishedPosts(
            @LoginMember(anonymous = true, allowInvalidToken = true) Member member,
            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "publishedAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "id", direction = Sort.Direction.DESC)
            }) Pageable pageable
    ) {
        return blogService.getPublishedPosts(member, pageable);
    }

    @Override
    @GetMapping("/posts/{postId}/summary")
    public BlogPostSummaryResponse getPublishedPostSummary(
            @LoginMember(anonymous = true, allowInvalidToken = true) Member member,
            @PathVariable @Positive(message = "postId는 1 이상의 값이어야 합니다.") Long postId
    ) {
        return blogService.getPublishedPostSummary(postId);
    }

    @Override
    @GetMapping("/posts/{postId}")
    public BlogPostDetailResponse getPublishedPostDetail(
            @LoginMember(anonymous = true, allowInvalidToken = true) Member member,
            @PathVariable @Positive(message = "postId는 1 이상의 값이어야 합니다.") Long postId
    ) {
        return blogService.getPublishedPostDetail(postId, member);
    }

    @Override
    @GetMapping("/categories")
    public List<BlogCategoryResponse> getBlogCategories() {
        return blogService.getBlogCategories();
    }
}
