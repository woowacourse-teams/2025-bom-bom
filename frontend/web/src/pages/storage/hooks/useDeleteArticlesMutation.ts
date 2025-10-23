import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteArticle } from '@/apis/articles';
import { queries } from '@/apis/queries';

interface UseDeleteArticlesMutationProps {
  date?: string;
}

export const useDeleteArticlesMutation = ({
  date,
}: UseDeleteArticlesMutationProps = {}) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (articleIds: number[]) =>
      deleteArticle({ articleIds: articleIds }),
    onSuccess: () => {
      if (date) {
        queryClient.invalidateQueries({
          queryKey: queries.articles({ date }).queryKey,
        });
      } else {
        queryClient.invalidateQueries({
          queryKey: ['articles'],
        });
      }
    },
  });
};
