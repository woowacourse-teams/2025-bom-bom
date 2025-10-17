import { infiniteQueryOptions, queryOptions } from '@tanstack/react-query';
import {
  getArticleById,
  getArticles,
  getArticlesStatisticsNewsletters,
  type GetArticleByIdParams,
  type GetArticlesParams,
  type GetArticleStatisticsNewslettersParams,
} from './articles';
import { getSignupCheck } from './auth';
import {
  getArticleBookmarkStatus,
  getBookmarks,
  getBookmarksStatisticsNewsletters,
} from './bookmark';
import { getHighlights, getHighlightStatisticsNewsletter } from './highlight';
import {
  getMonthlyReadingRank,
  getMyMonthlyReadingRank,
  getReadingStatus,
  getUserInfo,
  getMyNewsletters,
  getUserProfile,
} from './members';
import { getNewsletterDetail, getNewsletters } from './newsLetters';
import {
  getPreviousArticleDetail,
  getPreviousArticles,
} from './previousArticles';
import type { GetSignupCheckParams } from './auth';
import type {
  GetArticleBookmarkStatusParams,
  GetBookmarksParams,
} from './bookmark';
import type { GetHighlightsParams } from './highlight';
import type { GetMonthlyReadingRankParams } from './members';
import type { GetNewsletterDetailParams } from './newsLetters';
import type {
  GetPreviousArticleDetailParams,
  GetPreviousArticlesParams,
} from './previousArticles';

export const queries = {
  // articles
  articles: (params?: GetArticlesParams) =>
    queryOptions({
      queryKey: ['articles', params],
      queryFn: () => getArticles(params ?? {}),
    }),

  infiniteArticles: (params?: GetArticlesParams) =>
    infiniteQueryOptions({
      queryKey: ['articles', 'infinite', params],
      queryFn: ({ pageParam }) =>
        getArticles({
          ...(params ?? {}),
          page: typeof pageParam === 'number' ? pageParam : 0,
        }),
      getNextPageParam: (lastPage) => {
        if (!lastPage) return undefined;
        if (lastPage.last) return undefined;
        return (lastPage.number ?? 0) + 1;
      },
      initialPageParam: 0,
    }),

  articleById: (params: GetArticleByIdParams) =>
    queryOptions({
      queryKey: ['articles', params.id],
      queryFn: () => getArticleById(params),
    }),

  articlesStatisticsNewsletters: (
    params: GetArticleStatisticsNewslettersParams,
  ) =>
    queryOptions({
      queryKey: ['articles', 'statistics', 'newsletters', params],
      queryFn: () => getArticlesStatisticsNewsletters(params),
    }),

  // members
  me: () =>
    queryOptions({
      queryKey: ['members', 'me'],
      queryFn: getUserInfo,
    }),

  userProfile: () =>
    queryOptions({
      queryKey: ['members', 'me', 'profile'],
      queryFn: getUserProfile,
      retry: false,
    }),

  readingStatus: () =>
    queryOptions({
      queryKey: ['members', 'me', 'reading'],
      queryFn: getReadingStatus,
    }),

  monthlyReadingRank: (params: GetMonthlyReadingRankParams) =>
    queryOptions({
      queryKey: ['members', 'me', 'reading', 'month', 'rank', params],
      queryFn: () => getMonthlyReadingRank(params),
    }),

  myMonthlyReadingRank: () =>
    queryOptions({
      queryKey: ['members', 'me', 'reading', 'month', 'rank', 'me'],
      queryFn: () => getMyMonthlyReadingRank(),
    }),

  // newsletters
  newsletters: () =>
    queryOptions({
      queryKey: ['newsletters'],
      queryFn: getNewsletters,
      staleTime: 1000 * 60 * 60 * 24 * 3, // 3 days
      gcTime: 1000 * 60 * 60 * 24 * 7, // 7 days
    }),

  myNewsletters: () =>
    queryOptions({
      queryKey: ['newsletters', 'me'],
      queryFn: getMyNewsletters,
    }),

  newsletterDetail: (params: GetNewsletterDetailParams) =>
    queryOptions({
      queryKey: ['newsletters', params.id],
      queryFn: () => getNewsletterDetail(params),
    }),

  // highlights
  highlights: (params?: GetHighlightsParams) =>
    queryOptions({
      queryKey: ['highlights', params],
      queryFn: () => getHighlights(params ?? {}),
    }),

  // bookmarks
  bookmarks: (params?: GetBookmarksParams) =>
    queryOptions({
      queryKey: ['bookmarks', params],
      queryFn: () => getBookmarks(params),
    }),

  articleBookmarkStatus: (params: GetArticleBookmarkStatusParams) =>
    queryOptions({
      queryKey: ['bookmarks', 'status', 'articles', params.articleId],
      queryFn: () => getArticleBookmarkStatus(params),
    }),

  bookmarksStatisticsNewsletters: () =>
    queryOptions({
      queryKey: ['bookmarks', 'statistics', 'newsletters'],
      queryFn: getBookmarksStatisticsNewsletters,
    }),

  highlightStatisticsNewsletter: () =>
    queryOptions({
      queryKey: ['highlights', 'statistics', 'newsletters'],
      queryFn: getHighlightStatisticsNewsletter,
    }),

  // auth
  signupCheck: (params: GetSignupCheckParams) =>
    queryOptions({
      queryKey: ['auth', 'signup', 'check', params],
      queryFn: () => getSignupCheck(params),
      enabled: false,
    }),

  // previous articles
  previousArticles: (params: GetPreviousArticlesParams) =>
    queryOptions({
      queryKey: ['articles', 'previous', params],
      queryFn: () => getPreviousArticles(params),
    }),

  previousArticleDetail: (params: GetPreviousArticleDetailParams) =>
    queryOptions({
      queryKey: ['articles', 'previous', params],
      queryFn: () => getPreviousArticleDetail(params),
    }),
};
