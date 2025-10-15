import { fetcher } from './fetcher';

export type GetPreviousArticlesParams = {
  newsletterId: number;
  limit?: number;
};
export type GetPreviousArticlesResponse = {
  articleId: number;
  title: string;
  contentsSummary: string;
  expectedReadTime: number;
}[];

export const getPreviousArticles = async (
  params: GetPreviousArticlesParams,
) => {
  return await fetcher.get<GetPreviousArticlesResponse>({
    path: '/articles/previous',
    query: params,
  });
};

export type GetPreviousArticleDetailParams = {
  articleId: number;
};
export type GetPreviousArticleDetailResponse = {
  id: number;
  title: string;
  contents: string;
  arrivedDateTime: string;
  expectedReadTime: number;
  newsletter: {
    name: string;
    email: string;
    imageUrl: string;
    category: string;
  };
};

export const getPreviousArticleDetail = async (
  params: GetPreviousArticleDetailParams,
) => {
  return await fetcher.get<GetPreviousArticleDetailResponse>({
    path: `/articles/previous/${params.articleId}`,
  });
};
