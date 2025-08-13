import { infiniteQueryOptions, queryOptions } from '@tanstack/react-query';
import {
  getArticleById,
  GetArticleByIdParams,
  getArticles,
  type GetArticlesParams,
  getStatisticsCategories,
  GetStatisticsCategoriesParams,
} from './articles';
import {
  getBookmarkArticles,
  getBookmarked,
  GetBookmarkedParams,
} from './bookmark';
import { getHighlights, GetHighlightsParams } from './highlight';
import { getReadingStatus, getUserInfo } from './members';
import { getNewsletters } from './newsLetters';

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
      queryFn: () => getArticles(params ?? {}),
      getNextPageParam: (lastPage, allPages) => {
        if (!lastPage?.totalPages) return undefined;
        const nextPage = allPages.length;
        return nextPage < lastPage.totalPages ? nextPage : undefined;
      },
      initialPageParam: 0,
    }),

  articleById: (params: GetArticleByIdParams) =>
    queryOptions({
      queryKey: ['articles', params.id],
      queryFn: () => getArticleById(params),
    }),

  statisticsCategories: (params?: GetStatisticsCategoriesParams) =>
    queryOptions({
      queryKey: ['articles', 'statistics', 'categories'],
      queryFn: () => getStatisticsCategories(params ?? {}),
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

  // newsletters
  newsletters: () =>
    queryOptions({
      queryKey: ['newsletters'],
      queryFn: getNewsletters,
    }),

  // highlights
  highlights: (params: GetHighlightsParams) =>
    queryOptions({
      queryKey: ['highlights', params?.articleId],
      queryFn: () => getHighlights(params),
    }),

  // bookmarks
  bookmarks: () =>
    queryOptions({
      queryKey: ['bookmarkArticles'],
      queryFn: () => getBookmarkArticles(),
    }),

  bookmarkStatus: (params: GetBookmarkedParams) =>
    queryOptions({
      queryKey: ['bookmarked', params.articleId],
      queryFn: () => getBookmarked(params),
    }),
};
