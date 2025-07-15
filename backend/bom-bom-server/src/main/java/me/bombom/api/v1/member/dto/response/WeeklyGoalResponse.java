package me.bombom.api.v1.member.dto.response;

import me.bombom.api.v1.member.domain.WeeklyGoal;

public record WeeklyGoalResponse(
        Long weeklyGoalId,
        int weeklyGoalCount
) {

    public static WeeklyGoalResponse from(WeeklyGoal weeklyGoal) {
        return new WeeklyGoalResponse(weeklyGoal.getId(), weeklyGoal.getWeeklyGoalCount());
    }
}
