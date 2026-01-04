package me.bombom.api.v1.challenge.repository;

import java.time.LocalDateTime;
import me.bombom.api.v1.challenge.domain.DailyGuideType;

public interface TodayDailyGuideRow {

    int getDayIndex();
    DailyGuideType getType();
    String getImageUrl();
    String getNotice();
    boolean isCommentEnabled();
    int getMyCommentExists();
    String getMyCommentContent();
    LocalDateTime getMyCommentCreatedAt();
}

