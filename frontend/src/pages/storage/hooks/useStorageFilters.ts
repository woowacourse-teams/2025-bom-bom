import { useQuery } from '@tanstack/react-query';
import { ChangeEvent, useCallback, useState } from 'react';
import { GetArticlesParams } from '@/apis/articles';
import { queries } from '@/apis/queries';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';

export const useStorageFilters = () => {
  const [selectedNewsletter, setSelectedNewsletter] = useState('전체');
  const [sortFilter, setSortFilter] = useState<'DESC' | 'ASC'>('DESC');
  const [searchInput, setSearchInput] = useState('');
  const debouncedSearchInput = useDebouncedValue(searchInput, 500);

  const baseQueryParams: GetArticlesParams = {
    sort: ['arrivedDateTime', sortFilter],
    keyword: debouncedSearchInput,
    size: 6,
    newsletter: selectedNewsletter === '전체' ? undefined : selectedNewsletter,
  };

  const { data: newsletterCounts } = useQuery(
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

  return {
    selectedNewsletter,
    sortFilter,
    searchInput,
    baseQueryParams,
    newsletterCounts,
    handleNewsletterChange,
    handleSortChange,
    handleSearchChange,
  };
};
