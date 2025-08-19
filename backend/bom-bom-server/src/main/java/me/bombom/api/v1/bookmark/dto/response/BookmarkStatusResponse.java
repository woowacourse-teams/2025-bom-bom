package me.bombom.api.v1.bookmark.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record BookmarkStatusResponse(
        @Schema(required = true)
        boolean bookmarkStatus
) {

    public static BookmarkStatusResponse from(boolean bookmarkStatus) {
        return new BookmarkStatusResponse(bookmarkStatus);
    }
}
