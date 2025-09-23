import { useQuery } from '@tanstack/react-query';
import { ChangeEvent, useCallback, useState } from 'react';
import { ARTICLE_SIZE } from '../constants/article';
import { GetArticlesParams } from '@/apis/articles';
import { queries } from '@/apis/queries';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';

export const useStorageFilters = () => {
  const [selectedNewsletterId, setSelectedNewsletterId] = useState<
    number | null
  >(null);
  const [sortFilter, setSortFilter] = useState<'DESC' | 'ASC'>('DESC');
  const [searchInput, setSearchInput] = useState('');
  const [page, setPage] = useState(1);
  const debouncedSearchInput = useDebouncedValue(searchInput, 500);

  const baseQueryParams: GetArticlesParams = {
    sort: ['arrivedDateTime', sortFilter],
    keyword: debouncedSearchInput,
    size: ARTICLE_SIZE,
    newsletterId: selectedNewsletterId ?? undefined,
    page,
  };

  const { data: newsletterCounts } = useQuery(
    queries.articlesStatisticsNewsletters({
      keyword: debouncedSearchInput,
    }),
  );

  const handleNewsletterChange = useCallback((id: number | null) => {
    setSelectedNewsletterId(id);
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
    setPage(1);
  }, []);

  return {
    selectedNewsletterId,
    sortFilter,
    searchInput,
    baseQueryParams,
    newsletterCounts,
    handleNewsletterChange,
    handleSortChange,
    handleSearchChange,
    handlePageChange,
    resetPage,
    page,
  };
};
