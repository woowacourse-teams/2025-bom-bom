import { useMutation, useQueryClient } from '@tanstack/react-query';
import { patchHighlight, PatchHighlightParams } from '@/apis/highlight';
import { queries } from '@/apis/queries';

export const useUpdateHighlightMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (params: PatchHighlightParams) => patchHighlight(params),

    onSuccess: (_, params) => {
      queryClient.invalidateQueries({
        queryKey: queries.highlights({ articleId: params.id }).queryKey,
      });
    },
  });
};
