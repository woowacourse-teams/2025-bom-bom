import { useQuery } from '@tanstack/react-query';
import { queries } from '@/apis/queries';
import type { GetArticlesWithSearchParams } from '@/apis/articles';

const useArticles = (params: GetArticlesWithSearchParams) => {
  const { keyword, ...paramsWithoutKeyword } = params;
  const hasKeyword = !!keyword;

  return useQuery(
    hasKeyword
      ? queries.articlesWithSearch(params)
      : queries.articles(paramsWithoutKeyword),
  );
};

export default useArticles;
