import { useQuery } from '@tanstack/react-query';
import { parseAsInteger, useQueryState } from 'nuqs';
import { useCallback, useEffect } from 'react';
import { queries } from '@/apis/queries';

export const useMemoFilters = () => {
  const [selectedNewsletterId, setSelectedNewsletterId] = useQueryState(
    'newsletterId',
    parseAsInteger.withDefault(0),
  );
  const [page, setPage] = useQueryState('page', parseAsInteger.withDefault(0));

  const baseQueryParams = {
    keyword: '',
    size: 12,
    newsletterId:
      selectedNewsletterId && selectedNewsletterId !== 0
        ? selectedNewsletterId
        : undefined,
    page,
  };

  const { data: newsletterCounts } = useQuery(
    queries.highlightStatisticsNewsletter(),
  );

  const handlePageChange = useCallback(
    (value: number) => {
      setPage(value);
    },
    [setPage],
  );

  const resetPage = useCallback(() => {
    setPage(0);
  }, [setPage]);

  // newsletterId가 변경될 때 page를 0으로 리셋
  useEffect(() => {
    resetPage();
  }, [selectedNewsletterId, resetPage]);

  return {
    selectedNewsletterId,
    baseQueryParams,
    newsletterCounts,
    handlePageChange,
    resetPage,
    page,
  };
};
