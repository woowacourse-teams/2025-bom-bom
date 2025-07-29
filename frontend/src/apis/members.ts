import { fetcher } from './fetcher';
import { components } from '@/types/openapi';

export const getReadingStatus = async () => {
  return await fetcher.get<components['schemas']['ReadingInformationResponse']>(
    {
      path: '/members/me/reading',
    },
  );
};

export const getWeeklyReadingGoal = async ({
  weeklyGoalCount,
  memberId,
}: components['schemas']['UpdateWeeklyGoalCountRequest']) => {
  return await fetcher.patch<
    components['schemas']['UpdateWeeklyGoalCountRequest'],
    components['schemas']['WeeklyGoalCountResponse']
  >({
    path: '/members/me/reading/progress/week/goal',
    body: {
      memberId,
      weeklyGoalCount,
    },
  });
};

export const patchWeeklyCount = async ({
  weeklyGoalCount,
  memberId,
}: components['schemas']['UpdateWeeklyGoalCountRequest']) => {
  return await fetcher.patch<
    components['schemas']['UpdateWeeklyGoalCountRequest'],
    components['schemas']['WeeklyGoalCountResponse']
  >({
    path: '/members/me/reading/progress/week/count',
    body: {
      memberId,
      weeklyGoalCount,
    },
  });
};

export const getUserInfo = async () => {
  return await fetcher.get<components['schemas']['MemberProfileResponse']>({
    path: '/members/me',
  });
};
