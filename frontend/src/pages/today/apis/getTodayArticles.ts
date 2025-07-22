import { fetcher } from '@/apis/fetcher';
import { PageableResponse } from '@/apis/types/PageableResponse';
import { Article } from '../types/article';

export const getTodayArticles = () => {
  return fetcher.get<PageableResponse<Article>>({
    path: '/articles',
    query: {
      date: new Date(),
      memberId: 1,
      sorted: 'DESC',
    },
  });
};
