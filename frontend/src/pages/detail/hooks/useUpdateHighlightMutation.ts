import { useMutation, useQueryClient } from '@tanstack/react-query';
import { patchHighlight, PatchHighlightParams } from '@/apis/highlight';

export const useUpdateHighlightMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (params: PatchHighlightParams) => patchHighlight(params),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['highlight'],
      });
    },
  });
};
