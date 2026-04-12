package me.bombom.api.v1.reading.dto;

public record MonthlyReadingRankFlat(

                String nickname,
                long rank,
                int monthlyReadCount,
                long nextRankDifference,
                String rankingBadgeGrade,
                Integer rankingBadgeYear,
                Integer rankingBadgeMonth,
                String challengeBadgeGrade,
                String challengeBadgeName,
                Integer challengeBadgeGeneration,
                String streakBadgeTier
) {

        public boolean hasRankingBadge() {
                return rankingBadgeGrade != null && rankingBadgeYear != null && rankingBadgeMonth != null;
        }

        public boolean hasChallengeBadge() {
                return challengeBadgeGrade != null && challengeBadgeName != null && challengeBadgeGeneration != null;
        }

        public boolean hasStreakBadge() {
                return streakBadgeTier != null;
        }
}
