package me.bombom.api.v1.bookmark.dto.response;

public record BookmarkStatusResponse(boolean bookmarkStatus) {

    public static BookmarkStatusResponse from(boolean bookmarkStatus) {
        return new BookmarkStatusResponse(bookmarkStatus);
    }
}
