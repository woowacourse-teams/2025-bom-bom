import { useMutation, useQueryClient } from '@tanstack/react-query';
import { addHighlightToDOM } from '../utils/highlight';
import { postHighlight } from '@/apis/highlight';
import { queries } from '@/apis/queries';
import type { Highlight } from '../types/highlight';
import type { PostHighlightParams } from '@/apis/highlight';

export const useAddHighlightMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (params: PostHighlightParams) => postHighlight(params),
    onSuccess: (addedHighlight, params) => {
      addHighlightToDOM(addedHighlight as Highlight);
      queryClient.invalidateQueries({
        queryKey: queries.highlights({ articleId: params.articleId }).queryKey,
      });
    },
  });
};
