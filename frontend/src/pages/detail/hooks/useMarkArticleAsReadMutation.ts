import { useMutation, useQueryClient } from '@tanstack/react-query';
import { patchArticleRead } from '@/apis/articles';
import { queries } from '@/apis/queries';

interface UseMarkArticleAsReadMutationParams {
  articleId: number;
}

const useMarkArticleAsReadMutation = ({
  articleId,
}: UseMarkArticleAsReadMutationParams) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => patchArticleRead({ id: articleId }),
    onSuccess: () => {
      const today = new Date();

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
