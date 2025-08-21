import { useInfiniteQuery } from '@tanstack/react-query';
import { GetArticlesParams } from '@/apis/articles';
import { queries } from '@/apis/queries';

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
