import { infiniteQueryOptions, queryOptions } from '@tanstack/react-query';
import {
  getArticleById,
  GetArticleByIdParams,
  getArticles,
  type GetArticlesParams,
  getArticlesStatisticsNewsletters,
  GetArticleStatisticsNewslettersParams,
} from './articles';
import {
  getArticleBookmarkStatus,
  GetArticleBookmarkStatusParams,
  getBookmarks,
  getBookmarksStatisticsNewsletters,
} from './bookmark';
import { getHighlights, GetHighlightsParams } from './highlight';
import { getReadingStatus, getUserInfo } from './members';
import {
  getNewsletterDetail,
  GetNewsletterDetailParams,
  getNewsletters,
} from './newsLetters';

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
  highlights: (params: GetHighlightsParams) =>
    queryOptions({
      queryKey: ['highlights', params?.articleId],
      queryFn: () => getHighlights(params),
    }),

  // bookmarks
  bookmarks: () =>
    queryOptions({
      queryKey: ['bookmarks'],
      queryFn: () => getBookmarks(),
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
};
