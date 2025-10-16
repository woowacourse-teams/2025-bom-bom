import { fetcher } from './fetcher';
import type { components, operations } from '@/types/openapi';

export type GetPreviousArticlesParams =
  operations['getPreviousArticles']['parameters']['query']['previousArticleRequest'];
export type GetPreviousArticlesResponse =
  components['schemas']['PreviousArticleResponse'][];

export const getPreviousArticles = async (
  params: GetPreviousArticlesParams,
) => {
  return await fetcher.get<GetPreviousArticlesResponse>({
    path: '/articles/previous',
    query: params,
  });
};

export type GetPreviousArticleDetailParams =
  operations['getPreviousArticleDetail']['parameters']['path'];
export type GetPreviousArticleDetailResponse =
  components['schemas']['PreviousArticleDetailResponse'];

export const getPreviousArticleDetail = async (
  params: GetPreviousArticleDetailParams,
) => {
  return await fetcher.get<GetPreviousArticleDetailResponse>({
    path: `/articles/previous/${params.id}`,
  });
};
