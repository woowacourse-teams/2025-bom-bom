import { infiniteQueryOptions, queryOptions } from '@tanstack/react-query';
import {
  getArticleById,
  getArticles,
  type GetArticlesParams,
  getArticlesStatisticsNewsletters,
,
  GetArticleByIdParams,
  GetArticleStatisticsNewslettersParams} from './articles';
import type { GetSignupCheckParams } from './auth';
import { getSignupCheck } from './auth';
import type {
  GetArticleBookmarkStatusParams,
  GetBookmarksParams,
} from './bookmark';
import {
  getArticleBookmarkStatus,
  getBookmarks,
  getBookmarksStatisticsNewsletters,
} from './bookmark';
import type { GetHighlightsParams } from './highlight';
import { getHighlights, getHighlightStatisticsNewsletter } from './highlight';
import type { GetMonthlyReadingRankParams } from './members';
import {
  getMonthlyReadingRank,
  getMyMonthlyReadingRank,
  getReadingStatus,
  getUserInfo,
} from './members';
import type { GetNewsletterDetailParams } from './newsLetters';
import { getNewsletterDetail, getNewsletters } from './newsLetters';

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
};
