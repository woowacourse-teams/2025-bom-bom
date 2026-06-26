package me.bombom.api.v1.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.member.dto.CategoryReadCount;

public record CategoryStatsResponse(

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        long total,

        @NotNull
        List<CategoryStatsItemResponse> categories
) {

    public static CategoryStatsResponse from(List<CategoryReadCount> categoryReadCounts) {
        long total = categoryReadCounts.stream()
                .mapToLong(CategoryReadCount::count)
                .sum();
        List<CategoryStatsItemResponse> categories = categoryReadCounts.stream()
                .map(categoryReadCount -> CategoryStatsItemResponse.of(categoryReadCount, total))
                .toList();
        return new CategoryStatsResponse(total, categories);
    }
}
