import { useQuery } from '@tanstack/react-query';
import { useMemo } from 'react';
import { queries } from '@/apis/queries';

interface UseInfiniteMemosParams {
  baseQueryParams: {
    sort: [string, 'DESC' | 'ASC'];
    keyword: string;
    size: number;
    newsletterId?: number;
    page: number;
  };
  isPc: boolean;
}

const useInfiniteMemos = ({
  baseQueryParams,
  isPc,
}: UseInfiniteMemosParams) => {
  const {
    data: highlights,
    isLoading,
    error,
  } = useQuery({
    ...queries.highlights(),
    enabled: !isPc,
  });

  const processedData = useMemo(() => {
    if (!highlights?.content) return null;

    const filteredHighlights = highlights.content.filter((highlight) => {
      const matchesNewsletter =
        !baseQueryParams.newsletterId ||
        highlight.id === baseQueryParams.newsletterId;

      return matchesNewsletter;
    });

    const sortedHighlights = filteredHighlights.sort((a, b) => {
      const dateA = new Date(a.createdAt ?? '').getTime();
      const dateB = new Date(b.createdAt ?? '').getTime();

      return baseQueryParams.sort[1] === 'DESC' ? dateB - dateA : dateA - dateB;
    });

    return sortedHighlights;
  }, [highlights, baseQueryParams]);

  return {
    data: processedData,
    isLoading,
    error,
  };
};

export default useInfiniteMemos;
