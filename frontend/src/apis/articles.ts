import { fetcher } from './fetcher';
import { components, operations } from '@/types/openapi';

interface GetArticlesParams {
  date?: string;
  category?: string;
  keyword?: string;

  page?: number;
  size?: number;
  sort?: 'ASC' | 'DESC';
}

type GetArticlesResponse = components['schemas']['PageArticleResponse'];

export const getArticles = async (params: GetArticlesParams) => {
  return await fetcher.get<GetArticlesResponse>({
    path: '/articles',
    query: { ...params },
  });
};

type GetArticleByIdParams =
  operations['getArticleDetail']['parameters']['path'];
type GetArticleByIdResponse = components['schemas']['ArticleDetailResponse'];

export const getArticleById = async ({ id }: GetArticleByIdParams) => {
  return await fetcher.get<GetArticleByIdResponse>({
    path: `/articles/${id}`,
  });
};

type PatchArticleReadParams = operations['updateIsRead']['parameters']['path'];

export const patchArticleRead = async ({ id }: PatchArticleReadParams) => {
  return await fetcher.patch({
    path: `/articles/${id}/read`,
  });
};

interface GetStatisticsCategoriesParams {
  keyword?: string;
}

export const getStatisticsCategories = async ({
  keyword,
}: GetStatisticsCategoriesParams) => {
  return await fetcher.get<
    components['schemas']['GetArticleCategoryStatisticsResponse']
  >({
    path: '/articles/statistics/categories',
    query: { keyword },
  });
};
