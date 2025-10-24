import { useQuery } from '@tanstack/react-query';
import { useSearch } from '@tanstack/react-router';
import { useCallback, useEffect, useState } from 'react';
import { queries } from '@/apis/queries';

export const useMemoFilters = () => {
  const { newsletterId: newsletterIdParams } = useSearch({
    from: '/_bombom/memo',
  });
  const [page, setPage] = useState(0);

  const baseQueryParams = {
    keyword: '',
    size: 12,
    newsletterId: newsletterIdParams,
    page,
  };

  const { data: newsletterCounts } = useQuery(
    queries.highlightStatisticsNewsletter(),
  );

  const handlePageChange = useCallback((value: number) => {
    setPage(value);
  }, []);

  const resetPage = useCallback(() => {
    setPage(0);
  }, [setPage]);

  // newsletterId가 변경될 때 page를 0으로 리셋
  useEffect(() => {
    resetPage();
  }, [resetPage]);

  return {
    baseQueryParams,
    newsletterCounts,
    handlePageChange,
    resetPage,
    page,
  };
};
