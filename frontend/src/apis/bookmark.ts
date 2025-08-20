import { fetcher } from './fetcher';
import { components, operations } from '@/types/openapi';

export type GetBookmarksParams = Omit<
  operations['getBookmarks']['parameters']['query'],
  'pageable'
> &
  components['schemas']['Pageable'];
export type GetBookmarksResponse = components['schemas']['PageArticleResponse'];

export const getBookmarks = async (params?: GetBookmarksParams) => {
  return await fetcher.get<GetBookmarksResponse>({
    path: '/bookmarks',
    query: params,
  });
};

export type GetArticleBookmarkStatusParams =
  operations['getBookmarkStatus']['parameters']['path'];
export type GetArticleBookmarkStatusResponse =
  components['schemas']['BookmarkStatusResponse'];

export const getArticleBookmarkStatus = async ({
  articleId,
}: GetArticleBookmarkStatusParams) => {
  return await fetcher.get<GetArticleBookmarkStatusResponse>({
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

export type GetBookmarksStatisticsNewslettersResponse =
  components['schemas']['BookmarkNewsletterStatisticsResponse'];

export const getBookmarksStatisticsNewsletters = async () => {
  return await fetcher.get<GetBookmarksStatisticsNewslettersResponse>({
    path: '/bookmarks/statistics/newsletters',
  });
};
