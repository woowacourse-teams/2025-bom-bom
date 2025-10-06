import type { ReadingKingRank } from './ReadingKingLeaderboard.types';

export const getLeaderboardData = (
  data: ReadingKingRank,
  rankingUnit: number,
) => {
  const leaderboardLength = Math.ceil(data.length / rankingUnit);

  const leaderboardData: ReadingKingRank[] = Array.from(
    { length: leaderboardLength },
    () => [],
  );

  data.forEach((rankData, index) => {
    const leaderboardIndex = Math.floor(index / rankingUnit);
    leaderboardData[leaderboardIndex]?.push(rankData);
  });

  return leaderboardData;
};
