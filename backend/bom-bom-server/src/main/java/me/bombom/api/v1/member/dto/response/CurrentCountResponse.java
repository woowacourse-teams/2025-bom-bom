package me.bombom.api.v1.member.dto.response;

import me.bombom.api.v1.member.domain.WeeklyGoal;

public record CurrentCountResponse(
        Long weeklyGoalId,
        int currentCount
) {

    public static CurrentCountResponse from(WeeklyGoal weeklyGoal) {
        return new CurrentCountResponse(weeklyGoal.getId(), weeklyGoal.getCurrentCount());
    }
}
