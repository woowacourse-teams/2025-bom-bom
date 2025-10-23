import { useQuery } from '@tanstack/react-query';
import { useCallback, useState } from 'react';
import { queries } from '@/apis/queries';

export const useMemoFilters = () => {
  const [selectedNewsletterId, setSelectedNewsletterId] = useState<
    number | null
  >(null);
  const [page, setPage] = useState(0);

  const baseQueryParams = {
    keyword: '',
    size: 12,
    newsletterId: selectedNewsletterId
      ? Number(selectedNewsletterId)
      : undefined,
    page,
  };

  const { data: newsletterCounts } = useQuery(
    queries.highlightStatisticsNewsletter(),
  );

  const handleNewsletterChange = useCallback((id: number | null) => {
    setSelectedNewsletterId(id);
  }, []);

  const handlePageChange = useCallback((value: number) => {
    setPage(value);
  }, []);

  const resetPage = useCallback(() => {
    setPage(0);
  }, []);

  return {
    selectedNewsletterId,
    baseQueryParams,
    newsletterCounts,
    handleNewsletterChange,
    handlePageChange,
    resetPage,
    page,
  };
};
