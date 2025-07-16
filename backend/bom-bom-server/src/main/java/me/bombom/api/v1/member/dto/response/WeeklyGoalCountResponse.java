package me.bombom.api.v1.member.dto.response;

import me.bombom.api.v1.member.domain.WeeklyReading;

public record WeeklyGoalCountResponse(
        Long weeklyReadingId,
        int weeklyGoalCount
) {

    public static WeeklyGoalCountResponse from(WeeklyReading weeklyReading) {
        return new WeeklyGoalCountResponse(weeklyReading.getId(), weeklyReading.getGoalCount());
    }
}
