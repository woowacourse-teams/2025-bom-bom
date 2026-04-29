package me.bombom.api.v1.reading.dto;

public record ContinueReadingRankFlat(

        String nickname,
        long rank,
        int dayCount,
        String rankingBadgeGrade,
        Integer rankingBadgeYear,
        Integer rankingBadgeMonth,
        String challengeBadgeGrade,
        String challengeBadgeName,
        Integer challengeBadgeGeneration,
        Integer streakDayCount
) {

    public boolean hasRankingBadge() {
        return rankingBadgeGrade != null && rankingBadgeYear != null && rankingBadgeMonth != null;
    }

    public boolean hasChallengeBadge() {
        return challengeBadgeGrade != null && challengeBadgeName != null && challengeBadgeGeneration != null;
    }

    public boolean hasStreakBadge() {
        return streakDayCount != null;
    }
}
