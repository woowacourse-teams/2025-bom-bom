import { useMutation, useQueryClient } from '@tanstack/react-query';
import { patchArticleRead } from '@/apis/articles';
import { queries } from '@/apis/queries';
import { formatDate } from '@/utils/date';

interface UseArticleAsReadMutationParams {
  articleId: number;
}

const useArticleAsReadMutation = ({
  articleId,
}: UseArticleAsReadMutationParams) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => patchArticleRead({ id: articleId }),
    onSuccess: () => {
      const today = new Date();

      queryClient.invalidateQueries({
        queryKey: queries.articleById({ id: articleId }).queryKey,
      });
      queryClient.invalidateQueries({
        queryKey: queries.articles({ date: formatDate(today, '-') }).queryKey,
      });
    },
  });
};

export default useArticleAsReadMutation;
