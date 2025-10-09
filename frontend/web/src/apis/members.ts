import { fetcher } from './fetcher';
import type { components, operations } from '@/types/openapi';

type GetReadingStatusResponse =
  components['schemas']['ReadingInformationResponse'];

export const getReadingStatus = async () => {
  return await fetcher.get<GetReadingStatusResponse>({
    path: '/members/me/reading',
  });
};

type GetUserInfoResponse = components['schemas']['MemberProfileResponse'];

export const getUserInfo = async () => {
  return await fetcher.get<GetUserInfoResponse>({
    path: '/members/me',
  });
};

type PatchWeeklyReadingGoalParams =
  operations['updateWeeklyGoalCount']['parameters']['query'];
type PatchWeeklyReadingGoalResponse =
  components['schemas']['WeeklyGoalCountResponse'];

export const patchWeeklyReadingGoal = async ({
  weeklyGoalCount,
}: PatchWeeklyReadingGoalParams) => {
  return await fetcher.patch<
    PatchWeeklyReadingGoalParams,
    PatchWeeklyReadingGoalResponse
  >({
    path: '/members/me/reading/progress/week/goal',
    query: {
      weeklyGoalCount,
    },
  });
};

export type GetMonthlyReadingRankResponse =
  components['schemas']['MonthlyReadingRankResponse'][];
export type GetMonthlyReadingRankParams =
  operations['getMonthlyReadingRank']['parameters']['query'];

export const getMonthlyReadingRank = async (
  params: GetMonthlyReadingRankParams,
) => {
  return await fetcher.get<GetMonthlyReadingRankResponse>({
    path: '/members/me/reading/month/rank',
    query: params,
  });
};

export type GetMyMonthlyReadingRankResponse =
  components['schemas']['MemberMonthlyReadingRankResponse'];

export const getMyMonthlyReadingRank = async () => {
  return await fetcher.get<GetMyMonthlyReadingRankResponse>({
    path: '/members/me/reading/month/rank/me',
  });
};
