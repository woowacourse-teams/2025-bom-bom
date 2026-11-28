package me.bombom.api.v1.notice.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeTranslation;

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
        return from(notice, null);
    }

    public static NoticeResponse from(Notice notice, NoticeTranslation translation) {
        return new NoticeResponse(
                notice.getId(),
                notice.getNoticeCategory().getValue(),
                translation == null || translation.getTitle() == null ? notice.getTitle() : translation.getTitle(),
                translation == null || translation.getContent() == null ? notice.getContent() : translation.getContent(),
                notice.getCreatedAt()
        );
    }
}
