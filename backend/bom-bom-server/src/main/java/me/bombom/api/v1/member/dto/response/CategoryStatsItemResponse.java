package me.bombom.api.v1.member.dto.response;

import me.bombom.api.v1.member.dto.CategoryReadCount;

public record CategoryStatsItemResponse(
        Long id,
        String name,
        long count,
        int percent
) {

    public static CategoryStatsItemResponse of(CategoryReadCount categoryReadCount, long total) {
        int percent = total == 0 ? 0 : (int) Math.round(categoryReadCount.count() * 100.0 / total);
        return new CategoryStatsItemResponse(
                categoryReadCount.id(),
                categoryReadCount.name(),
                categoryReadCount.count(),
                percent
        );
    }
}
