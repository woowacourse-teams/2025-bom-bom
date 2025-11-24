import { useQuery } from '@tanstack/react-query';
import { isValidKeyword } from '../utils/isValidKeyword';
import { queries } from '@/apis/queries';
import type { GetArticlesWithSearchParams } from '@/apis/articles';

const useArticles = (params: GetArticlesWithSearchParams) => {
  const { keyword, ...paramsWithoutKeyword } = params;

  return useQuery(
    isValidKeyword(keyword)
      ? queries.articlesWithSearch(params)
      : queries.articles(paramsWithoutKeyword),
  );
};

export default useArticles;
