import { queryOptions } from '@tanstack/react-query';
import {
  getArticles,
  getArticleById,
  getStatisticsCategories,
} from './articles';
import { postSignup } from './auth';
import {
  getUserInfo,
  getReadingStatus,
  patchWeeklyReadingGoal,
} from './members';
import { getNewsletters } from './newsLetters';

export const queries = {
  // articles
  articles: (params?: Parameters<typeof getArticles>[0]) =>
    queryOptions({
      queryKey: ['articles', params],
      queryFn: () => getArticles(params ?? {}),
    }),

  articleById: (params: Parameters<typeof getArticleById>[0]) =>
    queryOptions({
      queryKey: ['article', params.id],
      queryFn: () => getArticleById(params),
    }),

  statisticsCategories: (
    params?: Parameters<typeof getStatisticsCategories>[0],
  ) =>
    queryOptions({
      queryKey: ['statistics', 'categories'],
      queryFn: () => getStatisticsCategories(params ?? {}),
    }),

  // members
  me: () =>
    queryOptions({ queryKey: ['me'], queryFn: getUserInfo, retry: false }),

  readingStatus: () =>
    queryOptions({
      queryKey: ['reading', 'status'],
      queryFn: getReadingStatus,
    }),

  weeklyReadingGoal: (params: Parameters<typeof patchWeeklyReadingGoal>[0]) =>
    queryOptions({
      queryKey: ['reading', 'weekly', 'goal'],
      queryFn: () => patchWeeklyReadingGoal(params),
    }),

  // newsletters
  newsletters: () =>
    queryOptions({
      queryKey: ['newsletters'],
      queryFn: getNewsletters,
    }),

  // auth
  signup: (params: Parameters<typeof postSignup>[0]) =>
    queryOptions({
      queryKey: ['auth', 'signup'],
      queryFn: () => postSignup(params),
    }),
};
