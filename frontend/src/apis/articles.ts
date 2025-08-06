import { fetcher } from './fetcher';
import { components, operations } from '@/types/openapi';

export interface GetArticlesParams {
  date?: Date;
  category?: string;
  keyword?: string;

  page?: number;
  size?: number;
  sort?: 'ASC' | 'DESC';
}

export type GetArticlesResponse = components['schemas']['PageArticleResponse'];

export const getArticles = async (params: GetArticlesParams) => {
  return await fetcher.get<GetArticlesResponse>({
    path: '/articles',
    query: { ...params },
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

export type GetStatisticsCategoriesParams = Omit<
  operations['getArticleCategoryStatistics']['parameters']['query'],
  'member'
>;
export type GetStatisticsCategoriesResponse =
  components['schemas']['GetArticleCategoryStatisticsResponse'];

export const getStatisticsCategories = async (
  params: GetStatisticsCategoriesParams,
) => {
  return await fetcher.get<GetStatisticsCategoriesResponse>({
    path: '/articles/statistics/categories',
    query: { ...params },
  });
};
