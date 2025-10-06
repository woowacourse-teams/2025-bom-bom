import { useInfiniteQuery } from '@tanstack/react-query';
import { queries } from '@/apis/queries';
import type { GetArticlesParams } from '@/apis/articles';

interface UseInfiniteArticlesParams {
  baseQueryParams: GetArticlesParams;
  isPc: boolean;
}

const useInfiniteArticles = ({
  baseQueryParams,
  isPc,
}: UseInfiniteArticlesParams) => {
  return useInfiniteQuery({
    ...queries.infiniteArticles({
      ...baseQueryParams,
      newsletterId: baseQueryParams.newsletterId || undefined,
    }),
    enabled: !isPc,
  });
};

export default useInfiniteArticles;
