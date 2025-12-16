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

    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getNoticeCategory().getValue(),
                notice.getTitle(),
                notice.getContent(),
                notice.getCreatedAt()
        );
    }
}
