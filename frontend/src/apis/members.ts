import { fetcher } from './fetcher';
import { components } from '@/types/openapi';

export const getReadingStatus = async () => {
  return await fetcher.get<components['schemas']['ReadingInformationResponse']>(
    {
      path: '/members/me/reading',
    },
  );
};

export type GetUserInfoResponse =
  components['schemas']['MemberProfileResponse'];

export const getUserInfo = async () => {
  return await fetcher.get<GetUserInfoResponse>({
    path: '/members/me',
  });
};

export type PatchWeeklyReadingGoalParams =
  components['schemas']['UpdateWeeklyGoalCountRequest'];
export type PatchWeeklyReadingGoalResponse =
  components['schemas']['WeeklyGoalCountResponse'];

export const patchWeeklyReadingGoal = async ({
  weeklyGoalCount,
  memberId,
}: PatchWeeklyReadingGoalParams) => {
  return await fetcher.patch<
    PatchWeeklyReadingGoalParams,
    PatchWeeklyReadingGoalResponse
  >({
    path: '/members/me/reading/progress/week/goal',
    body: {
      memberId,
      weeklyGoalCount,
    },
  });
};
