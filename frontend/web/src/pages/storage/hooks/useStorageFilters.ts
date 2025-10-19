import { useSearch } from '@tanstack/react-router';
import { useCallback, useState } from 'react';
import { ARTICLE_SIZE } from '../constants/article';
import type { GetArticlesParams } from '@/apis/articles';
import type { ChangeEvent } from 'react';

export const useStorageFilters = () => {
  const param = useSearch({ from: '/_bombom/storage' });
  const [searchInput, setSearchInput] = useState('');
  const [page, setPage] = useState(1);

  const baseQueryParams: GetArticlesParams = {
    sort: ['arrivedDateTime', param.sort ?? 'DESC'],
    keyword: param.search,
    size: ARTICLE_SIZE,
    newsletterId: param.newsletterId ?? undefined,
    page,
  };

  const handleSearchChange = useCallback((e: ChangeEvent<HTMLInputElement>) => {
    setSearchInput(e.target.value);
  }, []);

  const handlePageChange = useCallback((value: number) => {
    setPage(value);
  }, []);

  const resetPage = useCallback(() => {
    setPage(1);
  }, []);

  return {
    sortFilter: param.sort,
    searchInput,
    baseQueryParams,
    handleSearchChange,
    handlePageChange,
    resetPage,
    page,
  };
};
