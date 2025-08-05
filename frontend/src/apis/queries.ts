import { queryOptions } from '@tanstack/react-query';
import { getArticles } from './articles';

export const queries = {
  articles: (params?: Parameters<typeof getArticles>[0]) =>
    queryOptions({
      queryKey: ['articles'],
      queryFn: () => getArticles(params ?? {}),
    }),
};
