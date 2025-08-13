import { queryOptions } from '@tanstack/react-query';
import {
  getArticleById,
  GetArticleByIdParams,
  getArticles,
  type GetArticlesParams,
  getArticlesStatisticsNewsletters,
} from './articles';
import {
  getArticleBookmarkStatus,
  GetArticleBookmarkStatusParams,
  getBookmarks,
  getBookmarksStatisticsNewsletters,
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

  articleById: (params: GetArticleByIdParams) =>
    queryOptions({
      queryKey: ['articles', params.id],
      queryFn: () => getArticleById(params),
    }),

  articlesStatisticsNewsletters: () =>
    queryOptions({
      queryKey: ['articles', 'statistics', 'newsletters'],
      queryFn: getArticlesStatisticsNewsletters,
    }),

  bookmarksStatisticsNewsletters: () =>
    queryOptions({
      queryKey: ['bookmarks', 'statistics', 'newsletters'],
      queryFn: getBookmarksStatisticsNewsletters,
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
      queryKey: ['bookmarks'],
      queryFn: () => getBookmarks(),
    }),

  articleBookmarkStatus: (params: GetArticleBookmarkStatusParams) =>
    queryOptions({
      queryKey: ['bookmarks', 'status', 'articles', params.articleId],
      queryFn: () => getArticleBookmarkStatus(params),
    }),
};
