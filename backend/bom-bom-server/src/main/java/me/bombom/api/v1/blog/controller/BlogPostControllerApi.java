package me.bombom.api.v1.blog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.blog.dto.response.BlogPostResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Blog", description = "블로그 관련 API")
public interface BlogPostControllerApi {

    @Operation(
            summary = "블로그 글 목록 조회",
            description = "발행된 블로그 글 목록을 조회합니다. 공개 글은 누구나 볼 수 있고, 비공개 글은 관리자만 볼 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "블로그 글 목록 조회 성공")
    })
    Page<BlogPostResponse> getPublishedPosts(
            @Parameter(hidden = true) @LoginMember(anonymous = true, allowInvalidToken = true) Member member,
            @Parameter(description = "페이징 관련 요청 (예: ?page=0&size=20&sort=publishedAt,desc)") Pageable pageable
    );
}
