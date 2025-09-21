import { useMutation, useQueryClient } from '@tanstack/react-query';
import { removeHighlightFromDOM } from '../utils/highlight';
import { deleteHighlight, DeleteHighlightParams } from '@/apis/highlight';

export const useRemoveHighlightMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (params: DeleteHighlightParams) => deleteHighlight(params),

    onSuccess: (_, params) => {
      removeHighlightFromDOM(params.id);
      queryClient.invalidateQueries({
        queryKey: ['highlight'],
      });
    },
  });
};
