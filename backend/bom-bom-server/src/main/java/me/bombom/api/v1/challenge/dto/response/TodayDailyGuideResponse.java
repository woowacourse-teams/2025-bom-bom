package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import me.bombom.api.v1.challenge.domain.DailyGuideType;

public record TodayDailyGuideResponse(

        @Schema(required = true)
        int dayIndex,

        @NotNull
        DailyGuideType type,

        @NotNull
        String imageUrl,

        String notice,

        @Schema(required = true)
        boolean commentEnabled,

        @NotNull
        MyCommentResponse myComment
) {

    public record MyCommentResponse(

            @Schema(required = true)
            boolean exists,

            String content,

            LocalDateTime createdAt
    ) {
    }
}

