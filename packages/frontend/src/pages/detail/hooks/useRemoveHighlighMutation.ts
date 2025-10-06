import { useMutation, useQueryClient } from '@tanstack/react-query';
import { removeHighlightFromDOM } from '../utils/highlight';
import { deleteHighlight } from '@/apis/highlight';
import { queries } from '@/apis/queries';
import type { DeleteHighlightParams } from '@/apis/highlight';

interface UseRemoveHighlightMutationParams {
  articleId: number;
}

export const useRemoveHighlightMutation = ({
  articleId,
}: UseRemoveHighlightMutationParams) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (params: DeleteHighlightParams) => deleteHighlight(params),

    onSuccess: (_, params) => {
      removeHighlightFromDOM(params.id);
      queryClient.invalidateQueries({
        queryKey: queries.highlights({ articleId }).queryKey,
      });
    },
  });
};
