import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Highlight } from '../types/highlight';
import { addHighlightToDOM, removeHighlightFromDOM } from '../utils/highlight';
import {
  deleteHighlight,
  patchHighlight,
  postHighlight,
  PostHighlightParams,
} from '@/apis/highlight';
import { queries } from '@/apis/queries';

export const useHighlightData = ({ articleId }: { articleId: number }) => {
  const queryClient = useQueryClient();
  const { data: highlights, isSuccess } = useQuery(
    queries.highlights({ articleId }),
  );

  const { mutate: addHighlight } = useMutation({
    mutationKey: ['addHighlights'],
    mutationFn: (params: PostHighlightParams) => postHighlight(params),
    onSuccess: (addedHighlight) => {
      addHighlightToDOM(addedHighlight as Highlight);
      queryClient.invalidateQueries({
        queryKey: ['highlight'],
      });
    },
  });
  const { mutate: updateHighlight } = useMutation({
    mutationKey: ['updateHighlight'],
    mutationFn: ({
      id,
      color,
      memo,
    }: {
      id: number;
      color?: string;
      memo?: string;
    }) => patchHighlight({ id, color, memo }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['highlight'],
      });
    },
  });
  const { mutate: removeHighlight } = useMutation({
    mutationKey: ['removeHighlight'],
    mutationFn: (id: number) => deleteHighlight({ id }),
    onSuccess: (_, id) => {
      removeHighlightFromDOM(id);
      queryClient.invalidateQueries({
        queryKey: ['highlight'],
      });
    },
  });

  const updateMemo = (id: number, memo: string) => {
    updateHighlight({ id, memo });
  };

  return {
    highlights: highlights?.content ?? [],
    isHighlightLoaded: isSuccess,
    addHighlight,
    updateMemo,
    removeHighlight,
  };
};
