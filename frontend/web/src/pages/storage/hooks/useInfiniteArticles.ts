import { useInfiniteQuery } from '@tanstack/react-query';
import { queries } from '@/apis/queries';
import type { GetArticlesWithSearchParams } from '@/apis/articles';

const useInfiniteArticles = (params: GetArticlesWithSearchParams) => {
  const { keyword, ...paramsWithoutKeyword } = params;
  const hasKeyword = !!keyword;

  return useInfiniteQuery(
    hasKeyword
      ? queries.infiniteArticlesWithSearch(params)
      : queries.infiniteArticles(paramsWithoutKeyword),
  );
};

export default useInfiniteArticles;
