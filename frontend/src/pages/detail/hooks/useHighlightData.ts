import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { HighlightType } from '../types/highlight';
import {
  deleteHighlight,
  getHighlights,
  patchHighlight,
  postHighlight,
} from '@/apis/highlight';

export const useHighlightData = ({ articleId }: { articleId: number }) => {
  const queryClient = useQueryClient();
  const { data: highlights } = useQuery({
    queryKey: ['highlight'],
    queryFn: () => getHighlights(articleId),
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
      queryClient.setQueryData<HighlightType[]>(['highlight'], (prev) => {
        if (!prev) return [];
        return prev.map((highlight) =>
          highlight.id === variables.id
            ? { ...highlight, ...variables.data }
            : highlight,
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

  return {
    highlights,
    addHighlight,
    updateMemo,
    removeHighlight,
  };
};
