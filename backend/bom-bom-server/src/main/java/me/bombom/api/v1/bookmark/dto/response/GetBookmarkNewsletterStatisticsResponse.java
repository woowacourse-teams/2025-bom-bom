package me.bombom.api.v1.bookmark.dto.response;

import java.util.List;

public record GetBookmarkNewsletterStatisticsResponse(
        int totalCount,
        List<GetBookmarkCountPerNewsletterResponse> newsletters
) {

    public static GetBookmarkNewsletterStatisticsResponse of(
            int totalCount,
            List<GetBookmarkCountPerNewsletterResponse> countResponse
    ) {
        return new GetBookmarkNewsletterStatisticsResponse(totalCount, countResponse);
    }
}
