import { fetcher } from './fetcher';
import { components, operations } from '@/types/openapi';

export type GetBookmarkArticlesResponse =
  components['schemas']['PageArticleResponse'];

export const getBookmarkArticles = async () => {
  return await fetcher.get<GetBookmarkArticlesResponse>({
    path: '/bookmarks',
  });
};

export type GetBookmarkedParams =
  operations['getBookmarkStatus']['parameters']['path'];
export type GetBookmarkedResponse =
  components['schemas']['BookmarkStatusResponse'];

export const getBookmarked = async ({ articleId }: GetBookmarkedParams) => {
  return await fetcher.get<GetBookmarkedResponse>({
    path: `/bookmarks/status/articles/${articleId}`,
  });
};

export type PostBookmarkParams =
  operations['addBookmark']['parameters']['path'];

export const postBookmark = async ({ articleId }: PostBookmarkParams) => {
  return await fetcher.post({
    path: `/bookmarks/articles/${articleId}`,
  });
};

export type DeleteBookmarkParams =
  operations['deleteBookmark']['parameters']['path'];

export const deleteBookmark = async ({ articleId }: DeleteBookmarkParams) => {
  return await fetcher.delete({
    path: `/bookmarks/articles/${articleId}`,
  });
};
