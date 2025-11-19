import { useInfiniteQuery } from '@tanstack/react-query';
import { isValidKeyword } from '../utils/isValidKeyword';
import { queries } from '@/apis/queries';
import type { GetArticlesWithSearchParams } from '@/apis/articles';

const useInfiniteArticles = (params: GetArticlesWithSearchParams) => {
  const { keyword, ...paramsWithoutKeyword } = params;

  return useInfiniteQuery(
    isValidKeyword(keyword)
      ? queries.infiniteArticlesWithSearch(params)
      : queries.infiniteArticles(paramsWithoutKeyword),
  );
};

export default useInfiniteArticles;
