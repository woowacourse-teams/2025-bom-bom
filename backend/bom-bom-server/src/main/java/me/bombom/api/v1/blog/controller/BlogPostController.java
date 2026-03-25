package me.bombom.api.v1.blog.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.blog.dto.response.BlogPostResponse;
import me.bombom.api.v1.blog.service.BlogService;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/blog/posts")
public class BlogPostController implements BlogPostControllerApi {

    private final BlogService blogService;

    @Override
    @GetMapping
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
}
