import { queryOptions } from '@tanstack/react-query';
import {
  getArticleById,
  getArticles,
  getStatisticsCategories,
} from './articles';
import { getReadingStatus, getUserInfo } from './members';
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
      queryKey: ['articles', 'statistics', 'categories'],
      queryFn: () => getStatisticsCategories(params ?? {}),
    }),

  // members
  me: () =>
    queryOptions({ queryKey: ['me'], queryFn: getUserInfo, retry: false }),

  readingStatus: () =>
    queryOptions({
      queryKey: ['members', 'me', 'reading'],
      queryFn: getReadingStatus,
    }),

  // newsletters
  newsletters: () =>
    queryOptions({
      queryKey: ['newsletters'],
      queryFn: getNewsletters,
    }),
};
