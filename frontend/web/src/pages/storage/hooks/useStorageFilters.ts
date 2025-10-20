import { useSearch } from '@tanstack/react-router';
import { useCallback, useState } from 'react';
import { ARTICLE_SIZE } from '../constants/article';
import type { GetArticlesParams } from '@/apis/articles';

export const useStorageFilters = () => {
  const param = useSearch({ from: '/_bombom/storage' });
  const [page, setPage] = useState(1);

  const baseQueryParams: GetArticlesParams = {
    sort: ['arrivedDateTime', param.sort ?? 'DESC'],
    keyword: param.search,
    size: ARTICLE_SIZE,
    newsletterId: param.newsletterId ?? undefined,
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
