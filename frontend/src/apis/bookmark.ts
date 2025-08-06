import { fetcher } from './fetcher';
import { components } from '@/types/openapi';

export const getBookmarkArticles = async () => {
  return await fetcher.get<components['schemas']['PageArticleResponse']>({
    path: '/bookmarks',
  });
};

interface GetBookmarkedParams {
  articleId: number;
}

export const getBookmarked = async ({ articleId }: GetBookmarkedParams) => {
  return await fetcher.get<components['schemas']['BookmarkStatusResponse']>({
    path: `/bookmarks/status/articles/${articleId}`,
  });
};

interface PostBookmarkParams {
  articleId: number;
}

export const postBookmark = async ({ articleId }: PostBookmarkParams) => {
  return await fetcher.post({
    path: `/bookmarks/articles/${articleId}`,
  });
};

interface DeleteBookmarkParams {
  articleId: number;
}

export const deleteBookmark = async ({ articleId }: DeleteBookmarkParams) => {
  return await fetcher.delete({
    path: `/bookmarks/articles/${articleId}`,
  });
};
