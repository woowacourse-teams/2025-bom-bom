import { fetcher } from './fetcher';
import { components } from '@/types/openapi';

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
  components['schemas']['UpdateWeeklyGoalCountRequest'];
type PatchWeeklyReadingGoalResponse =
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
