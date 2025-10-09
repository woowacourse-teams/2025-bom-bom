import { fetcher } from './fetcher';
import type { components, operations } from '@/types/openapi';

export type GetArticlesParams =
  components['schemas']['ArticlesOptionsRequest'] &
    components['schemas']['Pageable'];

export type GetArticlesResponse = components['schemas']['PageArticleResponse'];

export const getArticles = async (params: GetArticlesParams) => {
  return await fetcher.get<GetArticlesResponse>({
    path: '/articles',
    query: params,
  });
};

export type GetArticleByIdParams =
  operations['getArticleDetail']['parameters']['path'];
export type GetArticleByIdResponse =
  components['schemas']['ArticleDetailResponse'];

export const getArticleById = async ({ id }: GetArticleByIdParams) => {
  return await fetcher.get<GetArticleByIdResponse>({
    path: `/articles/${id}`,
  });
};

export type PatchArticleReadParams =
  operations['updateIsRead']['parameters']['path'];

export const patchArticleRead = async ({ id }: PatchArticleReadParams) => {
  return await fetcher.patch({
    path: `/articles/${id}/read`,
  });
};

export type GetArticleStatisticsNewslettersParams =
  operations['getArticleNewsletterStatistics']['parameters']['query']['request'];

export type GetArticlesStatisticsNewslettersResponse =
  components['schemas']['ArticleNewsletterStatisticsResponse'];

export const getArticlesStatisticsNewsletters = async (
  params: GetArticleStatisticsNewslettersParams,
) => {
  return await fetcher.get<GetArticlesStatisticsNewslettersResponse>({
    path: '/articles/statistics/newsletters',
    query: params,
  });
};
