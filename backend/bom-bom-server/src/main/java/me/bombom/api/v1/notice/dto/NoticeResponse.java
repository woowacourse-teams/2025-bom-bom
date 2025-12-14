package me.bombom.api.v1.notice.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import me.bombom.api.v1.notice.domain.Notice;

public record NoticeResponse(

        @NotNull
        Long noticeId,

        @NotNull
        String categoryName,

        @NotNull
        String title,

        @NotNull
        String content,

        @NotNull
        LocalDateTime createdAt
) {
}
