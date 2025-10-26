import { useSearch } from '@tanstack/react-router';
import { useCallback, useState } from 'react';
import { ARTICLE_SIZE } from '../constants/article';
import type { GetArticlesParams } from '@/apis/articles';

export const useStorageFilters = () => {
  const {
    sort: sortParam,
    search: searchParam,
    newsletterId: newsletterIdParams,
  } = useSearch({ from: '/_bombom/storage' });
  const [page, setPage] = useState(1);

  const baseQueryParams: GetArticlesParams = {
    sort: ['arrivedDateTime', sortParam ?? 'DESC'],
    keyword: searchParam,
    size: ARTICLE_SIZE,
    newsletterId: newsletterIdParams,
    page,
  };

  const handlePageChange = useCallback((value: number) => {
    setPage(value);
  }, []);

  const resetPage = useCallback(() => {
    setPage(1);
  }, []);

  return {
    baseQueryParams,
    handlePageChange,
    resetPage,
    page,
  };
};
