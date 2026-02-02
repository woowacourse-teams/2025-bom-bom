package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.badge.dto.response.BadgesResponse;
import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;

public record MonthlyReadingRankResponse(

        @NotNull
        String nickname,

        @Schema(required = true)
        long rank,

        @Schema(required = true)
        int monthlyReadCount,

        BadgesResponse badges
) {

    public static MonthlyReadingRankResponse from(MonthlyReadingRankFlat flat) {
        return new MonthlyReadingRankResponse(
                flat.nickname(),
                flat.rank(),
                flat.monthlyReadCount(),
                BadgesResponse.from(flat)
        );
    }

    public static List<MonthlyReadingRankResponse> from(List<MonthlyReadingRankFlat> flats) {
        return flats.stream()
                .map(MonthlyReadingRankResponse::from)
                .toList();
    }
}
