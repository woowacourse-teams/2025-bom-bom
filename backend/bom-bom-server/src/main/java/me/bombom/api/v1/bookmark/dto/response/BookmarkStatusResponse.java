package me.bombom.api.v1.bookmark.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record BookmarkStatusResponse(
        @Schema(type = "boolean", description = "북마크 상태", required = true)
        boolean bookmarkStatus
) {

    public static BookmarkStatusResponse from(boolean bookmarkStatus) {
        return new BookmarkStatusResponse(bookmarkStatus);
    }
}
