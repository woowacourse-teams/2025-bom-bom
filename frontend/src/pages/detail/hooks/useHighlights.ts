import { useQuery } from '@tanstack/react-query';
import { queries } from '@/apis/queries';

export const useHighlights = ({ articleId }: { articleId: number }) => {
  const { data: highlights, isSuccess } = useQuery(
    queries.highlights({ articleId }),
  );

  return {
    highlights: highlights?.content ?? [],
    isHighlightLoaded: isSuccess,
  };
};
