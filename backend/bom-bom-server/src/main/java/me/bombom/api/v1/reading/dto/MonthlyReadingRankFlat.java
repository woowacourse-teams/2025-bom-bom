package me.bombom.api.v1.reading.dto;

public record MonthlyReadingRankFlat(
        String nickname,
        long rank,
        int monthlyReadCount,
        String rankingBadgeGrade,
        Integer rankingBadgeYear,
        Integer rankingBadgeMonth,
        String challengeBadgeGrade,
        String challengeBadgeName,
        Integer challengeBadgeGeneration
) {
}
