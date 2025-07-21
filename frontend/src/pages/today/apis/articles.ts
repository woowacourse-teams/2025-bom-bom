import { fetcher } from '../../../apis/fetcher';
import { PageableResponse } from '../../../apis/types/PageableResponse';
import { Article } from '../types/article';

interface GetArticlesParams {
  date: Date;
  memberId: number;
  sorted: 'ASC' | 'DESC';
}

export const getArticles = async ({
  date,
  memberId,
  sorted,
}: GetArticlesParams) => {
  return await fetcher.get<PageableResponse<Article>>({
    path: '/articles',
    query: {
      date,
      memberId,
      sorted,
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
    query: { memberId },
  });
};

interface PatchArticleReadParams {
  articleId: number;
  memberId: number;
}

export const patchArticleRead = async ({
  articleId,
  memberId,
}: PatchArticleReadParams) => {
  return await fetcher.patch({
    path: `/articles/${articleId}/read`,
    body: {
      memberId,
    },
  });
};
