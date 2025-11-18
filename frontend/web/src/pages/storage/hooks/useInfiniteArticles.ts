import { useInfiniteQuery } from '@tanstack/react-query';
import { queries } from '@/apis/queries';
import type { GetArticlesWithSearchParams } from '@/apis/articles';

interface UseInfiniteArticlesParams {
  baseQueryParams: GetArticlesWithSearchParams;
  isPc: boolean;
}

const useInfiniteArticles = ({
  baseQueryParams,
  isPc,
}: UseInfiniteArticlesParams) => {
  const hasKeyword = !!baseQueryParams.keyword;
  return useInfiniteQuery({
    ...(hasKeyword
      ? queries.infiniteArticlesWithSearch(baseQueryParams)
      : queries.infiniteArticles(baseQueryParams)),
    enabled: !isPc,
  });
};

export default useInfiniteArticles;
