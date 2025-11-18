import { useQuery } from '@tanstack/react-query';
import { queries } from '@/apis/queries';
import type { GetArticlesWithSearchParams } from '@/apis/articles';

const useArticles = (params: GetArticlesWithSearchParams) => {
  const hasKeyword = !!params.keyword;
  return useQuery(
    hasKeyword ? queries.articlesWithSearch(params) : queries.articles(params),
  );
};

export default useArticles;
