import { useQuery } from '@tanstack/react-query';
import { ChangeEvent, useState } from 'react';
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

  const { data: categoryCounts } = useQuery(
    queries.articlesStatisticsNewsletters(),
  );

  const handleNewsletterChange = (value: string) => {
    setSelectedNewsletter(value);
  };

  const handleSortChange = (value: 'DESC' | 'ASC') => {
    setSortFilter(value);
  };

  const handleSearchChange = (e: ChangeEvent<HTMLInputElement>) => {
    setSearchInput(e.target.value);
  };

  const existNewsletters = categoryCounts?.newsletters.filter(
    (newsletter) => newsletter.count > 0,
  );

  return {
    selectedNewsletter,
    sortFilter,
    searchInput,
    debouncedSearchInput,
    baseQueryParams,
    categoryCounts,
    existNewsletters,
    handleNewsletterChange,
    handleSortChange,
    handleSearchChange,
  };
};
