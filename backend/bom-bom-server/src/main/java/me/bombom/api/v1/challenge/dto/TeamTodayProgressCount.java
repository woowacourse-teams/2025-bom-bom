package me.bombom.api.v1.challenge.dto;

public record TeamTodayProgressCount(

        long survivedCount,
        long completedTodayCount
) {
}
