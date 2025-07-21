import { fetcher } from '../../../apis/fetcher';
import { PageableResponse } from '../../../apis/types/PageableResponse';
import { Article } from '../types/article';

export const getTodayArticles = async () => {
  return await fetcher.get<PageableResponse<Article>>({
    path: '/articles',
    query: {
      date: new Date().toString(),
      category: '종합',
      page: '1',
    },
  });
};
