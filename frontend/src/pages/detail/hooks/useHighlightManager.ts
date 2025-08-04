import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { HighlightType } from '../types/highlight';
import { restoreHighlight } from '../utils/highlight';
import {
  deleteHighlight,
  getHighlights,
  patchHighlight,
  postHighlight,
} from '@/apis/highlight';

export const useHighlightManager = () => {
  const queryClient = useQueryClient();
  const { data: highlights } = useQuery({
    queryKey: ['highlight'],
    queryFn: () => getHighlights(),
  });
  const { mutate: addHighlight } = useMutation({
    mutationKey: ['addHighlights'],
    mutationFn: (highlight: Omit<HighlightType, 'id'>) =>
      postHighlight({ highlight }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['highlight'],
      });
    },
  });
  const { mutate: updateHighlight } = useMutation({
    mutationKey: ['updateHighlight'],
    mutationFn: ({
      id,
      data,
    }: {
      id: number;
      data: Partial<Omit<HighlightType, 'id'>>;
    }) => patchHighlight({ id, data }),
    onSuccess: (updatedHighlight, variables) => {
      queryClient.setQueryData<HighlightType[]>(['highlight'], (old) => {
        if (!old) return [];
        return old.map((h) =>
          h.id === variables.id ? { ...h, ...variables.data } : h,
        );
      });
    },
  });
  const { mutate: removeHighlight } = useMutation({
    mutationKey: ['removeHighlight'],
    mutationFn: (id: number) => deleteHighlight({ id }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['highlight'],
      });
    },
  });

  const updateMemo = (id: number, memo: string) => {
    updateHighlight({ id, data: { memo } });
  };

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
    addHighlight,
    updateMemo,
    removeHighlight,
  };
};
