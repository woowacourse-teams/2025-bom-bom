import { useQuery } from '@tanstack/react-query';
import { ChangeEvent, useCallback, useState } from 'react';
import { GetArticlesParams } from '@/apis/articles';
import { queries } from '@/apis/queries';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';

export const useStorageFilters = () => {
  const [selectedNewsletter, setSelectedNewsletter] = useState('전체');
  const [sortFilter, setSortFilter] = useState<'DESC' | 'ASC'>('DESC');
  const [searchInput, setSearchInput] = useState('');
  const [page, setPage] = useState(0);
  const debouncedSearchInput = useDebouncedValue(searchInput, 500);

  const baseQueryParams: GetArticlesParams = {
    sort: ['arrivedDateTime', sortFilter],
    keyword: debouncedSearchInput,
    size: 6,
    newsletter: selectedNewsletter === '전체' ? undefined : selectedNewsletter,
    page,
  };

  const { data: newletterCounts } = useQuery(
    queries.articlesStatisticsNewsletters({
      keyword: debouncedSearchInput,
    }),
  );

  const handleNewsletterChange = useCallback((value: string) => {
    setSelectedNewsletter(value);
  }, []);

  const handleSortChange = useCallback((value: 'DESC' | 'ASC') => {
    setSortFilter(value);
  }, []);

  const handleSearchChange = useCallback((e: ChangeEvent<HTMLInputElement>) => {
    setSearchInput(e.target.value);
  }, []);

  const handlePageChange = useCallback((value: number) => {
    setPage(value);
  }, []);

  const resetPage = useCallback(() => {
    setPage(0);
  }, []);

  return {
    selectedNewsletter,
    sortFilter,
    searchInput,
    baseQueryParams,
    newletterCounts,
    handleNewsletterChange,
    handleSortChange,
    handleSearchChange,
    handlePageChange,
    resetPage,
    page,
  };
};
