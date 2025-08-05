import { fetcher } from './fetcher';
import { components } from '@/types/openapi';

export const getReadingStatus = async () => {
  return await fetcher.get<components['schemas']['ReadingInformationResponse']>(
    {
      path: '/members/me/reading',
    },
  );
};

export type GetWeeklyReadingGoalParams =
  components['schemas']['UpdateWeeklyGoalCountRequest'];
export type GetWeeklyReadingGoalResponse =
  components['schemas']['WeeklyGoalCountResponse'];

export const getWeeklyReadingGoal = async ({
  weeklyGoalCount,
  memberId,
}: GetWeeklyReadingGoalParams) => {
  return await fetcher.patch<
    GetWeeklyReadingGoalParams,
    GetWeeklyReadingGoalResponse
  >({
    path: '/members/me/reading/progress/week/goal',
    body: {
      memberId,
      weeklyGoalCount,
    },
  });
};

export type PatchWeeklyCountParams =
  components['schemas']['UpdateWeeklyGoalCountRequest'];
export type PatchWeeklyCountResponse =
  components['schemas']['WeeklyGoalCountResponse'];

export const patchWeeklyCount = async ({
  weeklyGoalCount,
  memberId,
}: PatchWeeklyCountParams) => {
  return await fetcher.patch<PatchWeeklyCountParams, PatchWeeklyCountResponse>({
    path: '/members/me/reading/progress/week/count',
    body: {
      memberId,
      weeklyGoalCount,
    },
  });
};

export type GetUserInfoResponse =
  components['schemas']['MemberProfileResponse'];

export const getUserInfo = async () => {
  return await fetcher.get<GetUserInfoResponse>({
    path: '/members/me',
  });
};
