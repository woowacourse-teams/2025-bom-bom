import { useQuery } from '@tanstack/react-query';
import { useCallback, useState } from 'react';
import { queries } from '@/apis/queries';

export const useMemoFilters = () => {
  const [selectedNewsletterId, setSelectedNewsletterId] = useState<
    number | null
  >(null);
  const [sortFilter, setSortFilter] = useState<'DESC' | 'ASC'>('DESC');
  const [page, setPage] = useState(0);

  const baseQueryParams = {
    sort: ['createdAt', sortFilter],
    keyword: '',
    size: 6,
    newsletterId: selectedNewsletterId
      ? Number(selectedNewsletterId)
      : undefined,
    page,
  };

  const { data: newletterCounts } = useQuery(
    queries.highlightStatisticsNewsletter(),
  );

  const handleNewsletterChange = useCallback((id: number | null) => {
    setSelectedNewsletterId(id);
  }, []);

  const handleSortChange = useCallback((value: 'DESC' | 'ASC') => {
    setSortFilter(value);
  }, []);

  const handlePageChange = useCallback((value: number) => {
    setPage(value);
  }, []);

  const resetPage = useCallback(() => {
    setPage(0);
  }, []);

  return {
    selectedNewsletterId,
    sortFilter,
    baseQueryParams,
    newletterCounts,
    handleNewsletterChange,
    handleSortChange,
    handlePageChange,
    resetPage,
    page,
  };
};
