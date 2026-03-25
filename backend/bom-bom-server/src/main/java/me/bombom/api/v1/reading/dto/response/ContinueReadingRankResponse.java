package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.badge.dto.response.BadgesResponse;
import me.bombom.api.v1.reading.dto.ContinueReadingRankFlat;

public record ContinueReadingRankResponse(

        @NotNull
        String nickname,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        long rank,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int dayCount,

        BadgesResponse badges
) {

    public static ContinueReadingRankResponse from(ContinueReadingRankFlat flat) {
        return new ContinueReadingRankResponse(
                flat.nickname(),
                flat.rank(),
                flat.dayCount(),
                BadgesResponse.from(flat)
        );
    }

    public static List<ContinueReadingRankResponse> from(List<ContinueReadingRankFlat> flats) {
        return flats.stream()
                .map(ContinueReadingRankResponse::from)
                .toList();
    }
}
