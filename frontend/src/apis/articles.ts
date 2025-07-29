import { fetcher } from './fetcher';
import { PageableResponse } from './types/PageableResponse';
import { Article } from '../pages/today/types/article';
import { ArticleDetail } from '@/pages/detail/types/articleDetail';
import { CategoriesCountType } from '@/pages/today/types/category';

interface GetArticlesParams {
  sorted: 'ASC' | 'DESC';
  date?: Date;
  category?: string;
  size?: number;
  page?: number;
  keyword?: string;
}

export const getArticles = async ({
  date,
  sorted,
  category,
  size,
  page,
  keyword,
}: GetArticlesParams) => {
  return await fetcher.get<PageableResponse<Article>>({
    path: '/articles',
    query: {
      date,
      sorted,
      category,
      size,
      page,
      keyword,
    },
  });
};

interface GetArticleByIdParams {
  articleId: number;
}

export const getArticleById = async ({ articleId }: GetArticleByIdParams) => {
  return await fetcher.get<ArticleDetail>({
    path: `/articles/${articleId}`,
  });
};

interface PatchArticleReadParams {
  articleId: number;
}

export const patchArticleRead = async ({
  articleId,
}: PatchArticleReadParams) => {
  return await fetcher.patch({
    path: `/articles/${articleId}/read?`,
  });
};

interface GetStatisticsCategoriesParams {
  keyword?: string;
}

export const getStatisticsCategories = async ({
  keyword,
}: GetStatisticsCategoriesParams) => {
  return await fetcher.get<CategoriesCountType>({
    path: '/articles/statistics/categories',
    query: { keyword },
  });
};
