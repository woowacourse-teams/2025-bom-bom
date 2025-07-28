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

export const getReadingStatus = async () => {
  return await fetcher.get<MemberReadingResponse>({
    path: '/members/me/reading',
  });
};

type GetWeeklyReadingGoalParams = {
  weeklyGoalCount: string;
};

export interface PatchWeeklyGoalResponse {
  weeklyReadingId: number;
  weeklyGoalCount: number;
}

export const getWeeklyReadingGoal = async ({
  weeklyGoalCount,
}: GetWeeklyReadingGoalParams) => {
  return await fetcher.patch<
    GetWeeklyReadingGoalParams,
    PatchWeeklyGoalResponse
  >({
    path: '/members/me/reading/progress/week/goal',
    body: {
      weeklyGoalCount,
    },
  });
};

type PatchWeeklyCountParams = {
  count: number;
};

export interface PatchWeeklyCountResponse {
  weeklyReadingId: number;
  currentCount: number;
}

export const patchWeeklyCount = async ({ count }: PatchWeeklyCountParams) => {
  return await fetcher.patch<PatchWeeklyCountParams, PatchWeeklyCountResponse>({
    path: '/members/me/reading/progress/week/count',
    body: {
      count,
    },
  });
};

export interface UserInfoResponse {
  createdAt: string;
  updatedAt: string;
  id: number;
  provider: string;
  providerId: string;
  email: string;
  nickname: string;
  profileImageUrl: string;
  birthDate: string;
  gender: 'MALE' | 'FEMALE';
  roleId: number;
}

export const getUserInfo = async () => {
  return await fetcher.get<UserInfoResponse>({
    path: '/members/me',
  });
};
