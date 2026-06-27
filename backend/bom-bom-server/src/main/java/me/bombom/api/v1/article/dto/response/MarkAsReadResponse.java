package me.bombom.api.v1.article.dto.response;

public record MarkAsReadResponse(

        boolean readCountTokenConsumed
) {

    public static MarkAsReadResponse from(boolean readCounted) {
        return new MarkAsReadResponse(readCounted);
    }

    public static MarkAsReadResponse notCounted() {
        return new MarkAsReadResponse(false);
    }
}
