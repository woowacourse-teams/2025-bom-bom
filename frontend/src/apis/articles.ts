import { fetcher } from './fetcher';
import { PageableResponse } from './types/PageableResponse';
import { Article } from '../pages/today/types/article';

interface GetArticlesParams {
  date: Date;
  memberId: number;
  sorted: 'ASC' | 'DESC';
  category?: string;
  size?: number;
  page?: number;
}

export const getArticles = async ({
  date,
  memberId,
  sorted,
  category,
  size,
  page,
}: GetArticlesParams) => {
  return await fetcher.get<PageableResponse<Article>>({
    path: '/articles',
    query: {
      date,
      memberId,
      sorted,
      category,
      size,
      page,
    },
  });
};

interface GetArticleByIdParams {
  articleId: number;
  memberId: number;
}

export const getArticleById = async ({
  articleId,
  memberId,
}: GetArticleByIdParams) => {
  return await fetcher.get<Article>({
    path: `/articles/${articleId}`,
    query: { memberId: memberId.toString() },
  });
};

interface PatchArticleReadParams {
  articleId: number;
  memberId: number;
}

export const patchArticleRead = async ({
  articleId,
}: PatchArticleReadParams) => {
  return await fetcher.patch({
    path: `/articles/${articleId}/read`,
  });
};
