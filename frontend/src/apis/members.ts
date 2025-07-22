import { fetcher } from './fetcher';

export interface MemberReadingResponse {
  streakReadDay: number;
  today: {
    readCount: number;
    totalCount: number;
  };
  weekly: {
    readCount: number;
    goalCount: number;
  };
}

export const getReadingStatus = async (memberId: number) => {
  return await fetcher.get<MemberReadingResponse>({
    path: '/members/me/reading',
    query: { memberId },
  });
};

type GetWeeklyReadingGoalParams = {
  memberId: number;
  weeklyGoalCount: string;
};

export interface PatchWeeklyGoalResponse {
  weeklyReadingId: number;
  weeklyGoalCount: number;
}

export const getWeeklyReadingGoal = async ({
  memberId,
  weeklyGoalCount,
}: GetWeeklyReadingGoalParams) => {
  return await fetcher.patch<
    GetWeeklyReadingGoalParams,
    PatchWeeklyGoalResponse
  >({
    path: '/members/me/reading/progress/week/goal',
    body: {
      memberId,
      weeklyGoalCount,
    },
  });
};

type PatchWeeklyCountParams = {
  memberId: number;
  count: number;
};

export interface PatchWeeklyCountResponse {
  weeklyReadingId: number;
  currentCount: number;
}

export const patchWeeklyCount = async ({
  memberId,
  count,
}: PatchWeeklyCountParams) => {
  return await fetcher.patch<PatchWeeklyCountParams, PatchWeeklyCountResponse>({
    path: '/members/me/reading/progress/week/count',
    body: {
      memberId,
      count,
    },
  });
};
