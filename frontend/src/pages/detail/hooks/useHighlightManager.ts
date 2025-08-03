import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { HighlightType } from '../types/highlight';
import { restoreHighlight } from '../utils/highlight';
import { getHighlights, postHighlight } from '@/apis/highlight';

export const useHighlightManager = () => {
  const queryClient = useQueryClient();
  const { data: highlights } = useQuery({
    queryKey: ['highlight'],
    queryFn: () => getHighlights(),
  });

  const { mutate: addHighlights } = useMutation({
    mutationKey: ['addHighlights'],
    mutationFn: (highlight: Omit<HighlightType, 'id'>) =>
      postHighlight({ highlight }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['highlight'],
      });
    },
  });

  useEffect(() => {
    if (!highlights || highlights?.length === 0) return;

    highlights.forEach((h) => restoreHighlight(h));
  }, [highlights]);

  useEffect(() => {
    const handleMouseOver = (e: Event) => {
      const target = e.target as HTMLElement;
      if (target.tagName === 'MARK' && target.dataset.highlightId) {
        const id = target.dataset.highlightId;
        document
          .querySelectorAll(`mark[data-highlight-id="${id}"]`)
          .forEach((el) => el.classList.add('hovered-highlight'));
      }
    };

    const handleMouseOut = (e: Event) => {
      const target = e.target as HTMLElement;
      if (target.tagName === 'MARK' && target.dataset.highlightId) {
        const id = target.dataset.highlightId;
        document
          .querySelectorAll(`mark[data-highlight-id="${id}"]`)
          .forEach((el) => el.classList.remove('hovered-highlight'));
      }
    };

    document.addEventListener('mouseover', handleMouseOver);
    document.addEventListener('mouseout', handleMouseOut);

    return () => {
      document.removeEventListener('mouseover', handleMouseOver);
      document.removeEventListener('mouseout', handleMouseOut);
    };
  }, []);

  return {
    highlights,
    addHighlights,
  };
};
