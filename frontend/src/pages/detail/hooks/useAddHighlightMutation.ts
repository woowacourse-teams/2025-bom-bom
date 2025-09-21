import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Highlight } from '../types/highlight';
import { addHighlightToDOM } from '../utils/highlight';
import { postHighlight, PostHighlightParams } from '@/apis/highlight';

export const useAddHighlightMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (params: PostHighlightParams) => postHighlight(params),
    onSuccess: (addedHighlight) => {
      addHighlightToDOM(addedHighlight as Highlight);
      queryClient.invalidateQueries({
        queryKey: ['highlight'],
      });
    },
  });
};
