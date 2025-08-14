import { useQuery } from '@tanstack/react-query';
import { ChangeEvent, useCallback, useState } from 'react';
import { GetArticlesParams } from '@/apis/articles';
import { queries } from '@/apis/queries';
import { CategoryType } from '@/constants/category';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';

export const useStorageFilters = () => {
  const [selectedCategory, setSelectedCategory] =
    useState<CategoryType>('전체');
  const [sortFilter, setSortFilter] = useState<'DESC' | 'ASC'>('DESC');
  const [searchInput, setSearchInput] = useState('');
  const debouncedSearchInput = useDebouncedValue(searchInput, 500);

  const baseQueryParams: GetArticlesParams = {
    sort: `arrivedDateTime,${sortFilter}`,
    category: selectedCategory === '전체' ? undefined : selectedCategory,
    keyword: debouncedSearchInput,
    size: 6,
  };

  const { data: categoryCounts } = useQuery(
    queries.statisticsCategories({
      keyword: debouncedSearchInput,
    }),
  );

  const handleCategoryChange = useCallback((value: CategoryType) => {
    setSelectedCategory(value);
  }, []);

  const handleSortChange = useCallback((value: 'DESC' | 'ASC') => {
    setSortFilter(value);
  }, []);

  const handleSearchChange = useCallback((e: ChangeEvent<HTMLInputElement>) => {
    setSearchInput(e.target.value);
  }, []);

  const existCategories = categoryCounts?.categories?.filter(
    (category) => category.count !== 0,
  );

  return {
    selectedCategory,
    sortFilter,
    searchInput,
    debouncedSearchInput,
    baseQueryParams,
    categoryCounts,
    existCategories,
    handleCategoryChange,
    handleSortChange,
    handleSearchChange,
  };
};
