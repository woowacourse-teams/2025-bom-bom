package me.bombom.api.v1.bookmark.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.bookmark.dto.response.BookmarkResponse;
import me.bombom.api.v1.bookmark.dto.response.BookmarkStatusResponse;
import me.bombom.api.v1.bookmark.dto.response.GetBookmarkNewsletterStatisticsResponse;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Bookmark", description = "북마크 관련 API")
public interface BookmarkControllerApi {

    @Operation(
            summary = "북마크 목록 조회",
            description = "북마크 목록을 페이징하여 조회합니다. "
                    + "(정렬 기본값: ?page=0&size=10&sort=createdAt,desc)"

    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 정렬 파라미터 요청", content = @Content)
    })
    Page<BookmarkResponse> getBookmarks(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "페이징 관련 요청 (예: ?page=0&size=10&sort=createdAt,desc)") Pageable pageable
    );

    @Operation(
            summary = "북마크 상태 조회",
            description = "특정 아티클의 북마크 상태를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "북마크 상태 조회 성공")
    })
    BookmarkStatusResponse getBookmarkStatus(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "아티클 ID") @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId
    );

    @Operation(
            summary = "북마크 추가",
            description = "특정 아티클을 북마크에 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "북마크 추가 성공"),
            @ApiResponse(responseCode = "403", description = "아티클에 대한 접근 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "아티클을 찾을 수 없음", content = @Content)
    })
    void addBookmark(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "아티클 ID") @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId
    );

    @Operation(
            summary = "북마크 삭제",
            description = "특정 아티클을 북마크에서 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "북마크 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "아티클에 대한 접근 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "아티클을 찾을 수 없음", content = @Content)
    })
    void deleteBookmark(
            @Parameter(hidden = true) Member member,
            @Parameter(description = "아티클 ID") @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId
    );

    @Operation(
            summary = "뉴스레터별 북마크 개수 조회",
            description = "뉴스레터별 북마크 개수 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "뉴스레터별 개수 조회 성공")
    })
    GetBookmarkNewsletterStatisticsResponse getBookmarkNewsletterStatistics(
            @Parameter(hidden = true) Member member
    );
}
