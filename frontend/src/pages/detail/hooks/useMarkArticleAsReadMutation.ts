import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useMemo } from 'react';
import { patchArticleRead } from '@/apis/articles';
import { queries } from '@/apis/queries';

interface UseMarkArticleAsReadMutationParams {
  articleId: number;
}

const useMarkArticleAsReadMutation = ({
  articleId,
}: UseMarkArticleAsReadMutationParams) => {
  const queryClient = useQueryClient();
  const today = useMemo(() => new Date(), []);

  return useMutation({
    mutationKey: ['read', articleId],
    mutationFn: () => patchArticleRead({ id: articleId }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: queries.articleById({ id: articleId }).queryKey,
      });
      queryClient.invalidateQueries({
        queryKey: queries.articles({ date: today }).queryKey,
      });
    },
  });
};

export default useMarkArticleAsReadMutation;
