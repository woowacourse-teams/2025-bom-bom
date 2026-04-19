package me.bombom.api.v1.blog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import me.bombom.api.v1.blog.dto.response.BlogCategoryResponse;
import me.bombom.api.v1.blog.dto.response.BlogPostDetailResponse;
import me.bombom.api.v1.blog.dto.response.BlogPostResponse;
import me.bombom.api.v1.blog.dto.response.BlogPostSummaryResponse;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;

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

    @Operation(
            summary = "블로그 글 요약 조회 (SEO 전용)",
            description = "발행된 블로그 글을 요약 형태로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "블로그 글 요약 조회 성공"),
            @ApiResponse(responseCode = "404", description = "블로그 글을 찾을 수 없음", content = @Content)
    })
    BlogPostSummaryResponse getPublishedPostSummary(
            @Parameter(hidden = true) @LoginMember(anonymous = true, allowInvalidToken = true) Member member,
            @Parameter(description = "블로그 글 ID")
            @PathVariable @Positive(message = "postId는 1 이상의 값이어야 합니다.") Long postId
    );

    @Operation(
            summary = "블로그 글 상세 조회",
            description = "특정 블로그 글의 상세 정보를 조회합니다. 비공개 글은 관리자만 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "블로그 글 상세 조회 성공"),
            @ApiResponse(responseCode = "403", description = "블로그 글에 대한 접근 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "블로그 글을 찾을 수 없음", content = @Content)
    })
    BlogPostDetailResponse getPublishedPostDetail(
            @Parameter(hidden = true) @LoginMember(anonymous = true, allowInvalidToken = true) Member member,
            @Parameter(description = "블로그 글 ID")
            @PathVariable @Positive(message = "postId는 1 이상의 값이어야 합니다.") Long postId
    );

    @Operation(
            summary = "블로그 카테고리 목록 조회",
            description = "블로그 카테고리 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "블로그 카테고리 목록 조회 성공")
    })
    List<BlogCategoryResponse> getBlogCategories();
}
