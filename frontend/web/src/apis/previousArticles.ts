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
