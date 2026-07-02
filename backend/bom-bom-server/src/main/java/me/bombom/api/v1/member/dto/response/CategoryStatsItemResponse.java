package me.bombom.api.v1.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.member.dto.CategoryReadCount;

public record CategoryStatsItemResponse(

        @NotNull
        Long id,

        @NotNull
        String name,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        long count,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
