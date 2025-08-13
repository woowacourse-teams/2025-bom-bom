import { useInfiniteQuery } from '@tanstack/react-query';
import { getArticles, GetArticlesParams } from '@/apis/articles';

interface UseInfiniteArticlesParams {
  baseQueryParams: GetArticlesParams;
  isPc: boolean;
}

export const useInfiniteArticles = ({
  baseQueryParams,
  isPc,
}: UseInfiniteArticlesParams) => {
  return useInfiniteQuery({
    queryKey: ['articles', 'infinite', baseQueryParams],
    queryFn: ({ pageParam = 0 }) =>
      getArticles({
        ...baseQueryParams,
        page: pageParam,
      }),
    getNextPageParam: (lastPage, allPages) => {
      if (!lastPage?.totalPages) return undefined;
      const nextPage = allPages.length;
      return nextPage < lastPage.totalPages ? nextPage : undefined;
    },
    initialPageParam: 0,
    enabled: !isPc,
  });
};
